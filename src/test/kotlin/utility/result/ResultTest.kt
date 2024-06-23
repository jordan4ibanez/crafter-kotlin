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
  }

  @Test
  fun isErr() {
    // Error
    assertTrue {
      error.isErr()
    }
  }

  @Test
  fun unwrap() {
    // Error
    assertThrows<UnwrapException> {
      error.unwrap()
    }
  }

  @Test
  fun expect() {
    // Error
    assertThrows<ExpectException> {
      error.expect("Should fail.")
    }
  }

  @Test
  fun unwrapOrDefault() {
    // Error
    assertDoesNotThrow {
      assertEquals(100, error.unwrapOrDefault(100))
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
  }

  @Test
  fun unwrapErr() {
    assertDoesNotThrow {
      error.unwrapErr()
    }
  }

  @Test
  fun expectErr() {
    assertDoesNotThrow {
      error.expectErr("This should not throw.")
    }
  }

  @Test
  fun conclusion() {
    println("=== Result Test Concluded ===")
  }
}