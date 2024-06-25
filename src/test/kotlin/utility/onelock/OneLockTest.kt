package utility.onelock

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import utility.safety_exceptions.ExpectException
import utility.safety_exceptions.MultipleOneLockLockException
import utility.safety_exceptions.UnwrapException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OneLockTest {

  // OneLock is designed for late-init replacement.
  // Therefore, we cannot init it in place.
  // So we will use a function to do so.
  private fun setUpLocked(): OneLock<Int> {
    val l = OneLock<Int>()
    l.lock(1)
    return l
  }

  private val locked = setUpLocked()
  private val unlocked = OneLock<Int>()

  @Test
  fun introduction() {
    println("=== Starting OneLock Test ===")
  }

  @Test
  fun lock() {
    // Locked
    assertThrows<MultipleOneLockLockException> {
      locked.lock(5)
    }

    // We cannot lock unlocked because it needs to be unlocked.
  }

  @Test
  fun isLock() {
    assertTrue {
      locked.isLocked()
    }
    assertFalse {
      unlocked.isLocked()
    }
  }

  @Test
  fun unwrap() {
    assertDoesNotThrow {
      locked.unwrap()
    }
    assertThrows<UnwrapException> {
      unlocked.unwrap()
    }
  }

  @Test
  fun expect() {
    assertDoesNotThrow {
      locked.expect("This should not throw exception.")
    }
    assertThrows<ExpectException> {
      unlocked.expect("This should throw exception.")
    }
  }

  @Test
  fun withLocked() {
    assertThrows<Exception> {
      locked.withLocked {
        throw Exception("This should throw exception.")
      }
    }
    assertDoesNotThrow {
      unlocked.withLocked {
        throw Exception("This should not throw exception.")
      }
    }
  }

  @Test
  fun withUnlocked() {
    assertDoesNotThrow {
      locked.withUnlocked {
        throw Exception("This should not throw exception.")
      }
    }

    assertThrows<Exception> {
      unlocked.withUnlocked {
        throw Exception("This should throw exception.")
      }
    }
  }

  @Test
  fun conclusion() {
    println("=== OneLock Test Concluded ===")
  }
}