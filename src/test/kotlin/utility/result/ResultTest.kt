package utility.result

import org.junit.jupiter.api.Test
import kotlin.test.assertFails


class ResultTest {

  @Test
  fun errUnwrap() {
    val x: Result<Int, Error> = ErrString("Uh oh")
    assertFails {
      x.unwrap()
    }
    assertFails {
      x.expect("Yep this is working.")
    }
  }

//
//  @Test
//  fun cool2() {
//    fun blah(): Result<Int, Throwable> {
//      return if (Math.random() > 0.5) {
//        Ok(1)
//      } else {
//        Err(NullPointerException("oops"))
//      }
//    }
//
//    var x: Option<Int> = None()
//
//    blah()
//      .withOk {
//        x = Some(it)
//      }
//      .withErr {
//        when (it) {
//          is NullPointerException -> println("yeah that's null")
//          else -> println("some kind of other nonsense happened")
//        }
//        println(it.message)
//        println("oops")
//        x = Some(0)
//      }
//
//    x.withSome {
//      println(it + 1)
//    }
//      .withNone {
//        println("you done goofed")
//      }
//
//    fun nah(): Result<Int, RuntimeException> {
//      return Err(RuntimeException("oops"))
//    }
//
//    nah().expectErr("wat")
//
//    val cule: Result<Int, Error> = ErrString("hi there")
//
//    assertFails {
//      cule.unwrap()
//    }
//  }
}