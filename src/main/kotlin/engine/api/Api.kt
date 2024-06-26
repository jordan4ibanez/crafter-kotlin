package engine.api

//import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory

import engine.block.block
import engine.file_helpers.getFileList
import engine.file_helpers.getFolderList
import engine.file_helpers.isFile
import engine.file_helpers.isFolder
import engine.model.Texture
import engine.texture_atlas.worldAtlas
import groovy.lang.Binding
import groovy.util.GroovyScriptEngine

/*
api works as a state machine.
*/

object api {

  private val sharedData = Binding()
  val engine = GroovyScriptEngine("./mods/")

//  note: val for now, var when main menu gets put it so it can be reloaded. Or maybe not. We'll see.

  private const val MOD_BASE_FOLDER = "./mods"

  /**Holds the current mod name for debugging purposes.*/
  private var currentModName = ""

  /**Holds the current mod file for debugging purposes.*/
  private var currentModFile = ""

  /**Holds the current mod folder for debugging purposes.*/
  private var currentModFolder = ""

  /**Holds the folder directory literal*/
  private var currentDirectoryLiteral = ""

  /** API elements do what they say on the tin*/
  private val onTick = ArrayList<(delta: Float) -> Unit>()

  //? note: Groovy modding api.

  fun registerOnTick(function: (delta: Float) -> Nothing) = onTick.add(function)

  internal fun doOnTick(delta: Float) = onTick.forEach { it(delta) }

  //? note: API initialization internals.

  fun initialize() {

    // Create the api.
    runFile("_api.groovy")

    loadTextures()

    loadMods()

    // This maps the block definitions to the world atlas that was just created.
    block.updateTextureCoords()
  }

  private fun loadMods() {
    getFolderList(MOD_BASE_FOLDER).forEach { thisFolder: String ->
      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
      currentModName = thisFolder
      currentModFolder = thisFolder
      currentDirectoryLiteral = "$MOD_BASE_FOLDER/$thisFolder"
      if (!isFolder(currentDirectoryLiteral)) throw RuntimeException("api: Something strange has gone wrong with loading mods.\nFolder $thisFolder does not exist.")

      //!todo: check mod.json existence here!
      //!fixme: implement config checker!!
      //!fixme: currentModName is set here!

      val currentMain = "$currentDirectoryLiteral/main.groovy"
      if (!isFile(currentMain)) throw RuntimeException("api: $currentModName does not contain a main.groovy!")
      runFile("$currentModName/main.groovy")
    }
  }

  private fun loadTextures() {
    getFolderList(MOD_BASE_FOLDER).forEach { thisFolder: String ->
      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
      currentModName = thisFolder
      currentModFolder = thisFolder
      currentDirectoryLiteral = "$MOD_BASE_FOLDER/$thisFolder"
      if (!isFolder(currentDirectoryLiteral)) throw RuntimeException("api: Something strange has gone wrong with loading textures.\nFolder $thisFolder does not exist.")
      loadBlockTextures()
      loadIndividualTextures()
    }

    // Finally, flush the world atlas into the GPU.
    Texture.create("worldAtlas", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())
  }

  private fun loadBlockTextures() {
    val textureDirectory = "$MOD_BASE_FOLDER/$currentModFolder/textures"
    if (!isFolder(textureDirectory)) {
      println("api: $currentModName has no textures folder. Skipping."); return
    }
    val blockTextureDirectory = "$textureDirectory/blocks"
    if (!isFolder(blockTextureDirectory)) {
      println("api: $currentModName has no block textures folder. Skipping."); return
    }
    getFileList(blockTextureDirectory)
      .filter { it.contains(".png") }
      .ifEmpty { println("api: $currentModName has no block textures in folder. Skipping."); return }
      .forEach { foundTexture: String ->
        worldAtlas.add(foundTexture, "$blockTextureDirectory/$foundTexture")
      }
  }

  private fun loadIndividualTextures() {
    val textureDirectory = "$MOD_BASE_FOLDER/$currentModFolder/textures"
    if (!isFolder(textureDirectory)) {
      println("api: $currentModName has no textures folder. Skipping."); return
    }
    getFileList(textureDirectory)
      .filter { it.contains(".png") }
      .ifEmpty { println("api: $currentModName has no textures in folder. Skipping."); return }
      .forEach { foundTexture: String ->
        Texture.create(foundTexture, "$textureDirectory/$foundTexture")
      }
  }

  fun getCurrentModDirectory(): String {
    return currentModFolder
  }

  fun runFile(fileLocation: String) {
    currentModFile = fileLocation
    try {
      engine.run(fileLocation, sharedData)
    } catch (e: Exception) {
      throw RuntimeException("(Groovy API error in $currentModFile):\n$e")
    }
  }

  fun dofile(fileLocation: String) {
    runFile("$fileLocation.groovy")
  }

  fun stringArrayOf(vararg args: String): Array<String> {
    return arrayOf(*args)
  }
}
