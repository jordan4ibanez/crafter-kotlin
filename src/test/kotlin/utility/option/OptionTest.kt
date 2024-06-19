package utility.option

import org.junit.Test

class OptionTest {

  @Test
  fun cool() {
    fun blah(): Option<Int> {
      return Some(1)
    }
  }
}