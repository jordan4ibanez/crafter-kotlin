package utility.option

/**
 * An Option for when you are unsure if a data type will contain Some or None.
 *
 * @param T is the type of the data contained in the Option.
 */
abstract class Option<T> protected constructor(t: T?) {

  private var value: T? = t

  /**
   * Unwrap the Option.
   *
   * @throws Error Will throw when you attempt to unwrap a None option.
   */
  fun unwrap(): T {
    return with(this.value) {
      return@with when (this) {
        null -> throw Error("Unwrapped None Option. Did you check if it is Some?")
        else -> this // Smart cast into <T>.
      }
    }
  }

  /**
   * Unwrap the Option. Throw a custom error message if it's None.
   * As the name suggests, use this when you expectedly need data to be Some.
   *
   * @param errorMessage A custom Error message to be thrown if the Option is None.
   * @throws Error Will throw your custom error message when you attempt to unwrap a None option.
   */
  fun expect(errorMessage: String): T {
    return with(this.value) {
      return@with when (this) {
        null -> throw Error(errorMessage)
        else -> this // Smart cast into <T>.
      }
    }
  }

  /**
   * Check if the Option is Some.
   */
  fun isSome(): Boolean {
    return this.value != null
  }

  /**
   * Check if the Option is None.
   */
  fun isNone(): Boolean {
    return this.value == null
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
    with(this.value) {
      when (this) {
        null -> {}
        else -> f(this)
      }
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
    with(this.value) {
      when (this) {
        null -> f()
        else -> {}
      }
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