package utility.option

import utility.safety_exceptions.ExpectException
import utility.safety_exceptions.UnwrapException

/**
 * An Option for when you are unsure if a data type will contain Some or None.
 *
 * @param T is the type of the data contained in the Option.
 */
abstract class Option<T> protected constructor(private val value: T?) {

  /**
   * Unwrap the Option.
   *
   * @throws Error Will throw UnwrapException when you attempt to unwrap a None option.
   */
  fun unwrap(): T {
    return when (this) {
      is Some -> with(this.value) {
        when (this) {
          null -> throw UnwrapException("Unwrapped None Option. Did you check if it is Some?")
          else -> this // Smart cast into <T>.
        }
      }

      else -> throw UnwrapException("Unwrapped None Option. Did you check if it is Some?")
    }
  }

  /**
   * Unwrap the Option. Throw a custom error message if it's None.
   * As the name suggests, use this when you expectedly need data to be Some.
   *
   * @param errorMessage A custom Error message to be thrown if the Option is None.
   * @throws Error Will throw ExpectException with your custom error message when you attempt to unwrap a None option.
   */
  fun expect(errorMessage: String): T {
    return when (this) {
      is Some -> this.unwrap() // Smart cast into <T>.
      else -> throw ExpectException(errorMessage)
    }
  }

  /**
   * Check if the Option is Some.
   */
  fun isSome(): Boolean {
    return this is Some
  }

  /**
   * Check if the Option is None.
   */
  fun isNone(): Boolean {
    return this is None
  }

  /**
   * Run a closure with the data encapsulated within the Option.
   *
   * If the data is None, this has no effect.
   *
   * @param f Closure to run if it is Some. Interacts with data.
   * @return This, making it chainable into withNone().
   */
  fun withSome(f: (t: T) -> Unit): Option<T> {
    when (this) {
      is Some -> f(this.unwrap()) // Smart cast into <T>.
    }
    return this
  }

  /**
   * Run a closure if the data encapsulated is None.
   *
   * If the data is Some, this has no effect.
   *
   * @param f Closure to run if it is None.
   * @return This, making it chainable into withSome().
   */
  fun withNone(f: () -> Unit): Option<T> {
    when (this) {
      is None -> f()
    }
    return this
  }
}

/**
 * An Option that has Some value in it.
 */
class Some<T>(t: T) : Option<T>(t)

/**
 * An Option that has nothing at all.
 */
class None<T> : Option<T>(null)

/**
 * Create Some or None based on the input given to it.
 *
 * This is mainly to piggyback Option onto other functions.
 */
fun <T> undecided(t: T?): Option<T> {
  return when (t) {
    null -> None()
    else -> Some(t)
  }
}