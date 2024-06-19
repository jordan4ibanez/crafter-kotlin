package utility.result

import utility.option.Option
import utility.option.undecided

abstract class Result<T, E : Throwable> protected constructor(ok: T?, err: E?) {

  private val ok: Option<T>
  private val err: Option<E>

  init {
    // Only one can be None. Only one can be Some.
    val okNull = (ok == null)
    val errNull = (err == null)
    if (okNull == errNull) {
      throw Error("A Result must have either an Ok or Err. Not both. Not neither.")
    }
    this.ok = undecided(ok)
    this.err = undecided(err)
  }

  fun isOkay(): Boolean {
    return this.ok.isSome()
  }

  fun isErr(): Boolean {
    return this.err.isNone()
  }

  fun expect(errorMessage: String): T {
    return when (this.ok.isSome()) {
      false -> throw Error(errorMessage)
      true -> this.ok.unwrap() // Smart cast into <T>.
    }
  }

  fun unwrap(): T {
    return when (this) {
      is Ok -> this.ok.unwrap()// Smart cast into <T>.
      else -> throw this.err.unwrap()
    }
  }

  fun unwrapOrDefault(default: T): T {
    return when (this) {
      is Ok -> default
      else -> this.ok.unwrap() // Smart cast into <T>.
    }
  }

  fun withOk(f: (t: T) -> Unit): Result<T, E> {
    when (this) {
      is Ok -> f(this.ok.unwrap())
    }
    return this
  }

  fun expectErr(errorMessage: String): E {
    return when (this) {
      is Ok -> throw Error(errorMessage)
      else -> this.err.unwrap() // Smart cast into <E>.
    }
  }

  fun unwrapErr(): E {
    return when (this) {
      is Ok -> throw Error(this.ok.toString())
      else -> this.err.unwrap() // Smart cast into <E>.
    }
  }

  fun withErr(f: (e: E) -> Unit): Result<T, E> {
    when (this) {
      is Err -> f(this.err.unwrap())
    }
    return this
  }
}

class Ok<T, E : Throwable>(ok: T) : Result<T, E>(ok, null)

class Err<T, E : Throwable>(err: E) : Result<T, E>(null, err)