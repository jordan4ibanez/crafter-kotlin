package engine

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptContext

object api {

  //note: val for now, var when main menu gets put it so it can be reloaded. Or maybe not. We'll see.
  private val javaScript = NashornScriptEngineFactory().getScriptEngine("--language=es6")!!
  private val bindings = javaScript.getBindings(ScriptContext.ENGINE_SCOPE)
  private val compiler = javaScript as Compilable
  private val invoker = javaScript as Invocable

  fun initialize() {

    javaScript.eval("""
      print("Hello, API!")
    """.trimIndent())

  }
}