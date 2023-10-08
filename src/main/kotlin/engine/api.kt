package engine

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Compilable
import javax.script.CompiledScript
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
  private const val MOD_PATH = "./mods/"
  /**Holds the current mod name for debugging purposes.*/
  private var currentModName = ""
  /**Holds the current mod file for debugging purposes.*/
  private var currentModFile = ""
  /**Holds the current mod folder for debugging purposes.*/
  private var currentModFolder = ""

  // One day.
//  private val tsCompiler = something

  fun initialize() {

    // Create the api.
    runFile("./api/api.js")

    loadTextures()

  }

  private fun loadTextures() {
    getFolderList(MOD_PATH).forEach { thisFolder: String ->
      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
      currentModName = thisFolder
      currentModFolder = "$MOD_PATH$thisFolder"
      if (!isFolder(currentModFolder)) throw RuntimeException("api: Something strange has gone wrong with loading textures.\nFolder $thisFolder does not exist.")
      loadBlockTextures()
      loadIndividualTextures()
    }
    //! todo: flush the texture atlas into a texture. "worldAtlas"

    // Finally, flush the world atlas into the GPU.
    texture.create("worldAtlas", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())
  }

  private fun loadBlockTextures() {
    val textureDirectory = "$currentModFolder/textures"
    if (!isFolder(textureDirectory)) { println("api: $currentModName has no textures folder. Skipping."); return }
    val blockTextureDirectory = "$textureDirectory/blocks"
    if (!isFolder(blockTextureDirectory)) { println("api: $currentModName has no block textures folder. Skipping.") ; return }
    getFileList(blockTextureDirectory)
      .filter { it.contains(".png") }
      .ifEmpty { println("api: $currentModName has no block textures in folder. Skipping."); return }
      .forEach { foundTexture: String ->
        worldAtlas.add(foundTexture, "$blockTextureDirectory/$foundTexture")
      }
  }

  private fun loadIndividualTextures() {

  }

  fun runFile(fileLocation: String) {
    setCurrentFile(fileLocation)
    runCode(getFileString(fileLocation))
  }

  fun runCode(rawCode: String) {
    try { javaScript.eval(rawCode) } catch (e: Exception) { throw RuntimeException("(Javascript API error in $currentModFile):\n$e") }
  }

  private fun invoke(functionName: String, vararg args: Any): Any {
    try { return invoker.invokeFunction(functionName, args) } catch (e: Exception) { throw RuntimeException("(Javascript API error in $currentModFile):\n$e") }
  }

  private fun setCurrentFile(fileLocation: String) {
    currentModFile = fileLocation
  }

  //note: These two probably aren't going to be used but I'm including them anyways just in case.

  fun compileFile(fileLocation: String): CompiledScript {
    return compileCode(getFileString(fileLocation))
  }

  fun compileCode(rawCode: String): CompiledScript {
    return try { compiler.compile(rawCode) } catch (e: Exception) { throw RuntimeException("(Javascript API error in $currentModFile):\n$e") }
  }



}
