package utility.option

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import utility.safety_exceptions.ExpectException
import utility.safety_exceptions.UnwrapException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OptionTest {

  private val nothing = None<Int>()
  private val something = Some(1)

  @Test
  fun introduction() {
    println("=== Starting Option Test ===")
  }

  @Test
  fun unwrap() {
    // Nothing
    assertThrows<UnwrapException> {
      nothing.unwrap()
    }
    // Something
    assertDoesNotThrow {
      something.unwrap()
    }
  }

  @Test
  fun expect() {
    // Nothing
    assertThrows<ExpectException> {
      nothing.expect("oops")
    }
    // Something
    assertDoesNotThrow {
      something.expect("this should be something")
    }
  }

  @Test
  fun isSome() {
    // Nothing
    assertFalse {
      nothing.isSome()
    }
    // Something
    assertTrue {
      something.isSome()
    }
  }

  @Test
  fun isNone() {
    // Nothing
    assertTrue {
      nothing.isNone()
    }
    // Something
    assertFalse {
      something.isNone()
    }
  }

  @Test
  fun withSome() {
    // Nothing
    assertDoesNotThrow {
      nothing.withSome {
        println(it)
      }
    }
    // Something
    assertDoesNotThrow {
      something.withSome {
        println(it)
      }
    }
  }

  @Test
  fun withNone() {
    // Nothing
    assertDoesNotThrow {
      nothing.withNone {
        println("That's nothing!")
      }
    }
    // Something
    assertDoesNotThrow {
      something.withNone {
        println("That's something!")
      }
    }
  }

  @Test
  fun whenTypeCheck() {
    // Nothing
    assertDoesNotThrow {
      when (nothing) {
        is None -> println("That's a None type!")
        else -> throw Error("Something has gone wrong with None.")
      }
    }
    // Something
    assertDoesNotThrow {
      when (something) {
        is Some -> println("That's a Some type!")
        else -> throw Error("Something has gone wrong with Some.")
      }
    }
  }

  @Test
  fun conclusion() {
    println("=== Option Test Concluded ===")
  }
}