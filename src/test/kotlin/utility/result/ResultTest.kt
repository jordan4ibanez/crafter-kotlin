package utility.result

import org.junit.Test
import utility.option.None
import utility.option.Option
import utility.option.Some
import kotlin.test.assertFails


class ResultTest {

  @Test
  fun name() {
    TODO("Not yet implemented")
  }

  @Test
  fun blof() {
    throw RuntimeException("sdfg")
  }

  @Test
  fun wr() {
    var x: Result<Int, Error> = ErrString("asdf")
    assert(true == false)
  }

  @Test
  fun cool2() {
    fun blah(): Result<Int, Throwable> {
      return if (Math.random() > 0.5) {
        Ok(1)
      } else {
        Err(NullPointerException("oops"))
      }
    }

    var x: Option<Int> = None()

    blah()
      .withOk {
        x = Some(it)
      }
      .withErr {
        when (it) {
          is NullPointerException -> println("yeah that's null")
          else -> println("some kind of other nonsense happened")
        }
        println(it.message)
        println("oops")
        x = Some(0)
      }

    x.withSome {
      println(it + 1)
    }
      .withNone {
        println("you done goofed")
      }

    fun nah(): Result<Int, RuntimeException> {
      return Err(RuntimeException("oops"))
    }

    nah().expectErr("wat")

    val cule: Result<Int, Error> = ErrString("hi there")

    assertFails {
      cule.unwrap()
    }

    assert(false)
  }
}