package utility.result

import org.junit.Test

class ResultTest {

  @Test
  fun cool() {
    fun blah(): Result<Int, Error> {
      if (Math.random() > 0.5) {
        return Ok(1)
      } else {
        return Err(Error("oops"))
      }
    }

    fun nah(): Result<Int, RuntimeException> {
      return Err(RuntimeException("oops"))
    }
  }
}