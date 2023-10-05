package engine

import groovy.lang.Binding
import groovy.lang.GroovyShell
import groovy.util.Eval
import groovy.util.GroovyScriptEngine
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.openjdk.nashorn.internal.objects.NativeArray
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptContext

/*
api works as a state machine.
*/

object api {

////  note: val for now, var when main menu gets put it so it can be reloaded. Or maybe not. We'll see.
//  private val javaScript = NashornScriptEngineFactory().getScriptEngine("--language=es6")!!
//  private val bindings = javaScript.getBindings(ScriptContext.ENGINE_SCOPE)
//  private val compiler = javaScript as Compilable
//  private val invoker = javaScript as Invocable
//
//  // One day
////  private val tsCompiler = something
//
  private val binding = Binding()
  private val groovy = GroovyScriptEngine(arrayOf("./api", "./mods"))

  fun initialize() {
//    Eval.me("""
//      println("That's groovy 8)")
//    """.trimIndent())

    groovy.run("./api.groovy", binding)
//
//    // create the api.
//    runFile("./api/api.js")
//
  }
//
//  fun runFile(fileLocation: String) {
//    runCode(getFileString(fileLocation))
//  }
//
//  fun runCode(rawCode: String) {
//    try { javaScript.eval(rawCode) } catch (e: Exception) { throw RuntimeException("(Javascript API error):\n$e") }
//  }
//
//  fun runCode(jsRawCode: NativeArray) {
//    println("output debug:")
//    println(jsRawCode.toString())
//  }
//
//  private fun invoke(functionName: String, vararg args: Any): Any {
//    try { return invoker.invokeFunction(functionName, args) } catch (e: Exception) { throw RuntimeException("(Javascript API error):\n$e") }
//  }

}
