package utility.one_lock

import utility.option.None
import utility.option.Option
import utility.option.Some
import utility.result.Err
import utility.result.Ok
import utility.result.Result

/**
 * OneLock is a container for holding data which can only be set once, or it throws an error.
 *
 * Consider this a general purpose alternative to Kotlin's "late-init".
 */
class OneLock<T> {

  private var data: Option<T> = None()

  fun set(t: T): Result<Int, Error> {
    return when (data) {
      is Some -> Err(Error("Cannot set value more than once."))
      else -> Ok(0)
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
}