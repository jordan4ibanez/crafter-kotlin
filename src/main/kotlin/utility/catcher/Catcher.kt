package utility.catcher

import utility.result.Err
import utility.result.Ok
import utility.result.Result

/**
 * Wrapper Java functions that can throw into a Result.
 *
 * @param t The closure to run.
 * @return The Result<T> of attempting to run the throwable function.
 */
fun <T> catcher(t: () -> T): Result<T> {
  return try {
    Ok(t())
  } catch (e: Throwable) {
    Err(e.message.toString())
  }
}