package utility.onelock

import utility.option.None
import utility.option.Option
import utility.option.Some
import utility.safety_exceptions.MultipleOneLockSetException

/**
 * OneLock is a container for holding data which can only be set once, or it throws an error.
 *
 * Consider this a general purpose alternative to Kotlin's "late-init".
 */
class OneLock<T> {

  private var data: Option<T> = None()

  fun set(t: T) {
    when (data) {
      is Some -> throw MultipleOneLockSetException("Cannot set value more than once.")
      else -> this.data = Some(t)
    }
  }

  fun isSet(): Boolean {
    return data is Some
  }

  fun unwrap(): T {
    return data.unwrap()
  }

  fun expect(errorMessage: String): T {
    return when (data) {
      is Some -> data.unwrap()
      else -> throw Error(errorMessage)
    }
  }

  fun withSome(f: (t: T) -> Unit): OneLock<T> {
    when (data) {
      is Some -> f(data.unwrap())
    }
    return this
  }

  fun withNone(f: () -> Unit): OneLock<T> {
    when (data) {
      is None -> f()
    }
    return this
  }
}