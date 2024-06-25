package utility.onelock

import utility.option.None
import utility.option.Option
import utility.option.Some
import utility.safety_exceptions.ExpectException
import utility.safety_exceptions.MultipleOneLockLockException
import utility.safety_exceptions.UnwrapException

/**
 * OneLock is a container for holding data which can only be locked once, or it throws an error.
 *
 * Consider this a general purpose alternative to Kotlin's "late-init".
 */
class OneLock<T> {

  private var data: Option<T> = None()

  /**
   * Lock the data of the OneLock.
   *
   * @param data The data to store in the OneLock.
   * @throws MultipleOneLockLockException If the data has already been locked.
   */
  fun lock(data: T) {
    when (this.data) {
      is Some -> throw MultipleOneLockLockException("Cannot lock OneLock more than once.")
      else -> this.data = Some(data)
    }
  }

  /**
   * Check if the data has been locked.
   *
   * @return If the data has been locked.
   */
  fun isLocked(): Boolean {
    return data is Some
  }

  /**
   * Unwrap the data.
   *
   * @throws UnwrapException If the data has not been locked.
   */
  fun unwrap(): T {
    return data.unwrap()
  }

  /**
   * Unwrap the data. Throw a custom error message if it has not been locked.
   *
   * @param errorMessage The custom error message.
   * @throws ExpectException If the data has not been locked.
   */
  fun expect(errorMessage: String): T {
    return data.expect(errorMessage)
  }

  /**
   * Run a lambda to interact with the data contained in the OneLock.
   * If there is no data, this has no effect.
   *
   * @param f Lambda to run on the data, if there is any.
   * @return The OneLock, for chaining with withNone().
   */
  fun withLocked(f: (t: T) -> Unit): OneLock<T> {
    when (data) {
      is Some -> f(data.unwrap())
    }
    return this
  }

  /**
   * Run a lambda when there is no data in the OneLock.
   * If there is data, this has no effect.
   *
   * @param f Lambda to run if there's no data.
   * @return The OneLock, for chaining with isSome().
   */
  fun withUnlocked(f: () -> Unit): OneLock<T> {
    when (data) {
      is None -> f()
    }
    return this
  }
}