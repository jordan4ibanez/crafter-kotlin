package utility.result

import org.junit.jupiter.api.Test


class ResultTest {

  val stringError = ErrString<Int, RuntimeException>("It failed to do the thing.")
  val errorTyped = Err<Int, Error>(Error("Uh oh."))
  val unknownErrorType: Err<Int, Throwable> = when ((0..3).random()) {
    0 -> Err(Error("Just a plain old error."))
    1 -> Err(RuntimeException("Did something during runtime."))
    2 -> Err(NullPointerException("That's pretty pointy."))
    3 -> Err(Exception("It was excepted."))
    else -> Err(UnknownError("You did something completely unknown."))
  }
  val okay = Ok<Int, Error>(1)

  @Test
  fun introduction() {
    println("=== Starting Result Test ===")
  }

  @Test
  fun debugRandom() {
//    println(typeOf(unknownErrorType.unwrapErr()))
  }

  @Test
  fun conclusion() {
    println("=== Result Test Concluded ===")
  }
}