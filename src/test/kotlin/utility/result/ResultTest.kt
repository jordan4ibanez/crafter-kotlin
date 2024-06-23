package utility.result

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import utility.safety_exceptions.ExpectException
import utility.safety_exceptions.UnwrapException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ResultTest {

  // We want a few Err objects to do specific tests on.
  private val stringError = Err<Int, RuntimeException>("It failed to do the thing.")
  private val errorTyped = Err<Int, Error>(Error("Uh oh."))
  private val unknownErrorType: Err<Int, Throwable> = when ((0..3).random()) {
    0 -> Err(Error("Just a plain old error."))
    1 -> Err(RuntimeException("Did something during runtime."))
    2 -> Err(NullPointerException("That's pretty pointy."))
    3 -> Err(Exception("It was excepted."))
    else -> Err(UnknownError("You did something completely unknown."))
  }

  // Then we just want a simple Ok to ensure random things don't fail during runtime.
  private val okay = Ok<Int, Error>(1)

  @Test
  fun introduction() {
    println("=== Starting Result Test ===")
  }

  @Test
  fun isOkay() {
    // String Error
    assertFalse {
      stringError.isOkay()
    }
  }

  @Test
  fun isErr() {
    // String Error
    assertTrue {
      stringError.isErr()
    }
  }

  @Test
  fun unwrap() {
    // String Error
    assertThrows<UnwrapException> {
      stringError.unwrap()
    }
  }

  @Test
  fun expect() {
    // String Error
    assertThrows<ExpectException> {
      stringError.expect("Should fail.")
    }
  }

  @Test
  fun unwrapOrDefault() {
    // String Error
    assertDoesNotThrow {
      assertEquals(100, stringError.unwrapOrDefault(100))
    }
  }

  @Test
  fun withOk() {
    // String Error
    assertDoesNotThrow {
      stringError.withOk {
        throw Error("Shouldn't work")
      }
    }
  }

  @Test
  fun debugRandom() {
    when (unknownErrorType.unwrapErr()) {
      is NullPointerException -> println("That's a null pointer.")
      is RuntimeException -> println("That's a runtime.")
      is Error -> println("That's an error.")
      is Exception -> println("That's an exception.")
    }
  }

  @Test
  fun conclusion() {
    println("=== Result Test Concluded ===")
  }
}