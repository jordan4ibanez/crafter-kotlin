package utility.result

import utility.option.Option
import utility.option.undecided

/**
 * A Result is a type that represents 2 states:
 * Ok - Success! Contains data.
 * Err - There was a failure. Contains throwable.
 *
 * This has been written, so you can also late throw or intercept failure states in functions.
 * Makes the code base more durable.
 */
abstract class Result<T, E : Throwable> protected constructor(ok: T?, err: E?) {

  /**
   * Only exists in Ok variant of Result. Represents successful return.
   */
  private val ok: Option<T>

  /**
   * Only exists in Err variant of Result. Represents failed return via a Throwable.
   */
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

  /**
   * Check if the Result is Ok.
   *
   * @return If it is Ok.
   */
  fun isOkay(): Boolean {
    return this is Ok
  }

  /**
   * Check if the Result is Err.
   *
   * @return If it is Err.
   */
  fun isErr(): Boolean {
    return this is Err
  }

  /**
   * Unwrap Result as Ok unchecked with custom error message if the Result is Err.
   *
   * @throws Error Your custom error message.
   * @return Whatever data T represents.
   */
  fun expect(errorMessage: String): T {
    return when (this) {
      is Ok -> this.ok.unwrap() // Smart cast into <T>.
      else -> throw Error(errorMessage)
    }
  }

  /**
   * Unwrap Result as Ok unchecked. Will throw the Err held if the Result is an Err.
   *
   * @throws Throwable The held error if it is an Err.
   * @return The data T represents.
   */
  fun unwrap(): T {
    return when (this) {
      is Ok -> this.ok.unwrap()// Smart cast into <T>.
      else -> throw this.err.unwrap()
    }
  }

  /**
   * Unwrap result as Ok. If the Result is an Err, swap for supplemented default.
   *
   * @param default The value to return if the Result is an Err.
   * @return The data T represents or default if the Result is an Err.
   */
  fun unwrapOrDefault(default: T): T {
    return when (this) {
      is Ok -> default
      else -> this.ok.unwrap() // Smart cast into <T>.
    }
  }

  /**
   * Run a lambda with the Ok value stored in the Result.
   * If the Result is Err, this has no effect.
   *
   * @param f The function to run.
   * @return The Result for further chaining.
   */
  fun withOk(f: (t: T) -> Unit): Result<T, E> {
    when (this) {
      is Ok -> f(this.ok.unwrap())
    }
    return this
  }

  /**
   * Unwrap Result as Err unchecked with custom error message if the Result is Ok.
   *
   * @throws Error Your custom error message.
   * @return The Throwable E represents.
   */
  fun expectErr(errorMessage: String): E {
    return when (this) {
      is Ok -> throw Error(errorMessage)
      else -> this.err.unwrap() // Smart cast into <E>.
    }
  }

  /**
   * Unwrap Result as Err unchecked. Will throw the Ok held if the Result is an Ok.
   *
   * @throws Throwable The held Ok if it is an Ok.
   * @return The throwable E represents.
   */
  fun unwrapErr(): E {
    return when (this) {
      is Ok -> throw Error(this.ok.toString())
      else -> this.err.unwrap() // Smart cast into <E>.
    }
  }

  /**
   * Run a lambda with the Err value stored in the Result.
   * If the Result is Ok, this has no effect.
   *
   * @param f The function to run.
   * @return The Result for further chaining.
   */
  fun withErr(f: (e: E) -> Unit): Result<T, E> {
    when (this) {
      is Err -> f(this.err.unwrap())
    }
    return this
  }
}

/**
 * Ok Result. Indicates successful function run. Contains type T.
 */
class Ok<T, E : Throwable>(ok: T) : Result<T, E>(ok, null)

// We do not want to accidentally use this at all.
@Suppress("ClassName")
open class __ZBaseErr<T, E : Throwable> protected constructor(err: E) : Result<T, E>(null, err)

/**
 * Err Result. Indicates failed function run. Contains Throwable type E.
 */
class Err<T, E : Throwable>(err: E) : __ZBaseErr<T, E>(err)

// Kotlin cannot understand that Throwable is the base class, so we must suppress this.
@Suppress("UNCHECKED_CAST")
class ErrString<T, E : Throwable>(err: String) : __ZBaseErr<T, E>(Throwable(err) as E)