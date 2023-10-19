package engine

//import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory

import groovy.lang.Binding
import groovy.util.GroovyScriptEngine
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory
import javax.script.Compilable
import javax.script.Invocable

/*
api works as a state machine.
*/

object api {

  private val sharedData = Binding()
  val engine = GroovyScriptEngine("./mods")

  fun initialize() {

    engine.run("crafter/main.groovy", sharedData)
//    val result = engine.eval(script)
//    println("the result of $script is $result")
  }

//
////  note: val for now, var when main menu gets put it so it can be reloaded. Or maybe not. We'll see.
//  private val javaScript = NashornScriptEngineFactory().getScriptEngine("--language=es6")!!
//  private val bindings = javaScript.getBindings(ScriptContext.ENGINE_SCOPE)
//  private val compiler = javaScript as Compilable
//  private val invoker = javaScript as Invocable
//  private const val MOD_PATH = "./mods/"
//  /**Holds the current mod name for debugging purposes.*/
//  private var currentModName = ""
//  /**Holds the current mod file for debugging purposes.*/
//  private var currentModFile = ""
//  /**Holds the current mod folder for debugging purposes.*/
//  private var currentModFolder = ""
//
//  // One day.
////  private val tsCompiler = something
//
//  fun initialize() {
//
//    // Create the api.
//    currentModFolder = "./api"
//    runFile("./api/api.js")
//
//    loadTextures()
//
//    loadMods()
//
//    // This maps the block definitions to the world atlas that was just created.
//    block.updateTextureCoords()
//
//  }
//
//  private fun loadMods() {
//    getFolderList(MOD_PATH).forEach { thisFolder: String ->
//      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
//      currentModName = thisFolder
//      currentModFolder = "$MOD_PATH$thisFolder"
//      if (!isFolder(currentModFolder)) throw RuntimeException("api: Something strange has gone wrong with loading mods.\nFolder $thisFolder does not exist.")
//
//      //!todo: check mod.json existence here!
//      //!fixme: implement config checker!!
//      //!fixme: currentModName is set here!
//
//      val currentMain = "$currentModFolder/main.js"
//      if (!isFile(currentMain)) throw RuntimeException("api: $currentModName does not contain a main.js!")
//      runFile(currentMain)
//    }
//  }
//
//  private fun loadTextures() {
//    getFolderList(MOD_PATH).forEach { thisFolder: String ->
//      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
//      currentModName = thisFolder
//      currentModFolder = "$MOD_PATH$thisFolder"
//      if (!isFolder(currentModFolder)) throw RuntimeException("api: Something strange has gone wrong with loading textures.\nFolder $thisFolder does not exist.")
//      loadBlockTextures()
//      loadIndividualTextures()
//    }
//
//    // Finally, flush the world atlas into the GPU.
//    texture.create("worldAtlas", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())
//  }
//
//  private fun loadBlockTextures() {
//    val textureDirectory = "$currentModFolder/textures"
//    if (!isFolder(textureDirectory)) { println("api: $currentModName has no textures folder. Skipping."); return }
//    val blockTextureDirectory = "$textureDirectory/blocks"
//    if (!isFolder(blockTextureDirectory)) { println("api: $currentModName has no block textures folder. Skipping.") ; return }
//    getFileList(blockTextureDirectory)
//      .filter { it.contains(".png") }
//      .ifEmpty { println("api: $currentModName has no block textures in folder. Skipping."); return }
//      .forEach { foundTexture: String ->
//        worldAtlas.add(foundTexture, "$blockTextureDirectory/$foundTexture")
//      }
//  }
//
//  private fun loadIndividualTextures() {
//    val textureDirectory = "$currentModFolder/textures"
//    if (!isFolder(textureDirectory)) { println("api: $currentModName has no textures folder. Skipping."); return }
//    getFileList(textureDirectory)
//      .filter { it.contains(".png") }
//      .ifEmpty { println("api: $currentModName has no textures in folder. Skipping."); return }
//      .forEach { foundTexture: String ->
//        texture.create(foundTexture, "$textureDirectory/$foundTexture")
//      }
//  }
//
//  fun getCurrentModDirectory(): String {
//    return currentModFolder
//  }
//
//  fun runFile(fileLocation: String) {
//    setCurrentFile(fileLocation)
//    runCode(getFileString(fileLocation))
//  }
//
//  fun runCode(rawCode: String) {
//    //? note: Import is great for getting the JSDoc working, but it crashes in Nashorn. Get it out.
//    val cleanedCode = rawCode
//      .split("\r?\n|\r".toRegex())
//      // There could be instances of "    import("blah")", fix this.
//      .filter { !it.trim().startsWith("import") }
//      .joinToString("\n")
//    try { javaScript.eval(cleanedCode) } catch (e: Exception) { throw RuntimeException("(Javascript API error in $currentModFile):\n$e") }
//  }
//
//  private fun invoke(functionName: String, vararg args: Any): Any {
//    try { return invoker.invokeFunction(functionName, args) } catch (e: Exception) { throw RuntimeException("(Javascript API error in $currentModFile):\n$e") }
//  }
//
//  private fun setCurrentFile(fileLocation: String) {
//    currentModFile = fileLocation
//  }
//
//  //note: These two probably aren't going to be used but I'm including them anyways just in case.
//
//  fun compileFile(fileLocation: String): CompiledScript {
//    return compileCode(getFileString(fileLocation))
//  }
//
//  fun compileCode(rawCode: String): CompiledScript {
//    return try { compiler.compile(rawCode) } catch (e: Exception) { throw RuntimeException("(Javascript API error in $currentModFile):\n$e") }
//  }
//


}
