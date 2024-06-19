package utility.result

import utility.option.Option
import utility.option.undecided

abstract class Result<T, E : Throwable> protected constructor(ok: T?, err: E?) {

  private val ok: Option<T>
  private val err: Option<E>

  init {
    // Only one can be None. Only one can be Some.
    if (ok == null && err == null) {
      throw Error("A Result must have either an Ok or Err.")
    } else if (ok != null && err != null) {
      throw Error("A Result must not contain both an Ok and an Err.")
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
    return when (this.ok) {
      null -> throw Error(errorMessage)
      else -> this.ok // Smart cast into <T>.
    }
  }

  fun unwrap(): T {
    return when (this.ok) {
      null -> when (this.err) {
        // This is now a safety check because we can't seal out extending this class.
        null -> throw Error("Result Err is missing an err field.")
        else -> throw this.err
      }

      else -> this.ok // Smart cast into <T>.
    }
  }

  fun unwrapOrDefault(default: T): T {
    return when (this.ok) {
      null -> default
      else -> this.ok // Smart cast into <T>.
    }
  }

  fun expectErr(errorMessage: String): E {
    return when (this.err) {
      null -> throw Error(errorMessage)
      else -> this.err // Smart cast into <E>.
    }
  }

  fun unwrapErr(): E {
    return when (this.err) {
      null -> throw Error(this.ok.toString())
      else -> this.err // Smart cast into <E>.
    }
  }

  fun withErr(f: (e: E) -> Unit) {

//    f(this.err)
  }
}

class Ok<T, E : Throwable>(ok: T) : Result<T, E>(ok, null)

class Err<T, E : Throwable>(err: E) : Result<T, E>(null, err)