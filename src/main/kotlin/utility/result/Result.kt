package utility.result

abstract class Result<T, E : Throwable> protected constructor(private val ok: T?, private val err: E?) {

  init {
    if (this.ok == null && this.err == null) {
      throw Error("A Result must have either an Ok or Err.")
    } else if (this.ok != null && this.err != null) {
      throw Error("A Result must not contain both an Ok and an Err.")
    }
  }

  fun isOkay(): Boolean {
    return this.ok != null
  }

  fun isErr(): Boolean {
    return this.err != null
  }

  fun expect(errorMessage: String): T {
    return when (this.ok) {
      null -> throw Error(errorMessage)
      else -> this.ok // Smart cast into <T>.
    }
  }

  fun unwrap(): T {
    return when (this.ok) {
      null -> when (this.err) {
        // This is now a safety check because we can't seal out extending this class.
        null -> throw Error("Result Err is missing an err field.")
        else -> throw this.err
      }

      else -> this.ok // Smart cast into <T>.
    }
  }

  fun unwrapOrDefault(default: T): T {
    return when (this.ok) {
      null -> default
      else -> this.ok // Smart cast into <T>.
    }
  }

  fun expectErr(errorMessage: String): E {
    return when (this.err) {
      null -> throw Error(errorMessage)
      else -> this.err // Smart cast into <E>.
    }
  }

  fun unwrapErr(): E {
    return when (this.err) {
      null -> throw Error(this.ok.toString())
      else -> this.err // Smart cast into <E>.
    }
  }
}

class Ok<T, E : Throwable>(ok: T) : Result<T, E>(ok, null)

class Err<T, E : Throwable>(err: E) : Result<T, E>(null, err)