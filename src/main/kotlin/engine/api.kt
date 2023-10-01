package engine

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptContext

/*
api works as a state machine.
*/

object api {

//  note: val for now, var when main menu gets put it so it can be reloaded. Or maybe not. We'll see.
  private val javaScript = NashornScriptEngineFactory().getScriptEngine("--language=es6")!!
  private val bindings = javaScript.getBindings(ScriptContext.ENGINE_SCOPE)
  private val compiler = javaScript as Compilable
  private val invoker = javaScript as Invocable

  // One day
//  private val tsCompiler =

  fun initialize() {

    // create the api.
    runFile("./api/api.js")

  }

  private fun runFile(fileLocation: String) {
    runCode(getFileString(fileLocation))
  }

  private fun runCode(rawCode: String) {
    try { javaScript.eval(rawCode) } catch (e: Exception) { throw RuntimeException("(Javascript API error):\n$e") }
  }

  private fun invoke(functionName: String, vararg args: Any): Any {
    try { return invoker.invokeFunction(functionName, args) } catch (e: Exception) { throw RuntimeException("(Javascript API error):\n$e") }
  }

  fun debugTest() {
    println("api debug successful.")
  }
}

fun test() {
  println("testFun")
}

class TestClass {
  val x = 1
}