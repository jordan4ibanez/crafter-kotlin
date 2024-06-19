package utility.result

abstract class Result<T, E : Throwable> protected constructor(private val ok: T?, private val err: E?) {

  fun isOkay(): Boolean {
    return this.ok != null
  }

  fun isErr(): Boolean {
    return this.err != null
  }

  fun expect(errorMessage: String): T {
    return with(this.ok) {
      return@with when (this) {
        null -> throw Error(errorMessage)
        else -> this // Smart cast into <T>.
      }
    }
  }

  fun unwrap(): T {
    return with(this.ok) {
      return@with when (this) {
        null -> throw Error("Unwrapped None Option. Did you check if it is Ok?")
        else -> this // Smart cast into <T>.
      }
    }
  }
}

class Ok<T>(ok: T) : Result<T, Nothing>(ok, null)

class Err<E : Throwable>(err: E) : Result<Nothing, E>(null, err)