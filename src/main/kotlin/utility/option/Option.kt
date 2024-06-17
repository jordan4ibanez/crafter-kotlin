package utility.option

/**
 * An Option for when you are unsure if a data type will contain Some or None.
 *
 * @param T is the type of the data contained in the Option.
 */
open class Option<T>(t: T?) {
  private var value: T? = t

  /**
   * Unwrap the Option.
   *
   * @throws Error Will throw if you blindly attempt to unwrap a None option.
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
   *
   * @param errorMessage A custom Error message to be thrown if the Option is None.
   * @throws Error Will throw if you blindly attempt to unwrap a None option.
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

class Some<T>(t: T) : Option<T>(t)

class None<T> : Option<T>(null)

fun boof(): Option<Int> {

  return Some(5)
}

fun test() {
  // This is an experiment with code styles.
  with(boof()) {
    when (this) {
      is Some -> {
        println("some")
        val x = this.unwrap()
        print(x + 1)
      }

      is None -> println("none")
    }
  }

  boof()
    .withSome {
      var x = it
      x += 1
      println(it)
    }
    .withNone {
      throw Error("test")
    }
}