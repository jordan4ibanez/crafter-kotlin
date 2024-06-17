package utility.option

/**
 * A result for when you are unsure if a data type will contain a variable.
 *
 * @param T is the type of the data contained in the shell.
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
   * Check if the Result is Some.
   */
  fun isSome(): Boolean {
    return this.value != null
  }

  /**
   * Check if the Result is None.
   */
  fun isNone(): Boolean {
    return this.value == null
  }

  /**
   * Run a closure with the data encapsulated within the Result.
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