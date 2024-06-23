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
  private val error = Err<Int>("It failed to do the thing.")

  // Then we just want a simple Ok to ensure random things don't fail during runtime.
  private val okay = Ok<Int>(1)

  @Test
  fun introduction() {
    println("=== Starting Result Test ===")
  }

  @Test
  fun isOkay() {
    // Error
    assertFalse {
      error.isOkay()
    }
    // Okay
    assertTrue {
      okay.isOkay()
    }
  }

  @Test
  fun isErr() {
    // Error
    assertTrue {
      error.isErr()
    }
    // Okay
    assertFalse {
      okay.isErr()
    }
  }

  @Test
  fun unwrap() {
    // Error
    assertThrows<UnwrapException> {
      error.unwrap()
    }
    // Okay
    assertDoesNotThrow {
      okay.unwrap()
    }
  }

  @Test
  fun expect() {
    // Error
    assertThrows<ExpectException> {
      error.expect("Should fail.")
    }
    // Okay
    assertDoesNotThrow {
      okay.expect("This should not fail.")
    }
  }

  @Test
  fun unwrapOrDefault() {
    // Error
    assertDoesNotThrow {
      assertEquals(100, error.unwrapOrDefault(100))
    }
    // Okay
    assertDoesNotThrow {
      assertEquals(1, okay.unwrapOrDefault(100))
    }
  }

  @Test
  fun withOk() {
    // Error
    assertDoesNotThrow {
      error.withOk {
        throw Error("This shouldn't work with an Error.")
      }
    }
    // Okay
    assertThrows<Error> {
      okay.withOk {
        throw Error("This should be throwing with an Okay")
      }
    }
  }

  @Test
  fun unwrapErr() {
    // Error
    assertDoesNotThrow {
      error.unwrapErr()
    }
    // Okay
    assertThrows<UnwrapException> {
      okay.unwrapErr()
    }
  }

  @Test
  fun expectErr() {
    // Error
    assertDoesNotThrow {
      error.expectErr("This should not throw.")
    }
    // Okay
    assertThrows<ExpectException> {
      okay.expectErr("This should throw.")
    }
  }

  @Test
  fun conclusion() {
    println("=== Result Test Concluded ===")
  }
}