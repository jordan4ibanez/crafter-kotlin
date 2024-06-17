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


}

class Some<T>(t: T) : Option<T>(t)

class None<T> : Option<T>(null)

fun boof(): Option<Int> {

  return Some(5)
}

fun test() {
  with(boof()) {
    when (this) {
      is Some -> println("some")
      is None -> println("none")
    }
  }
}