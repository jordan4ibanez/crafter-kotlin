package engine.api

import engine.block.Block
import engine.file_helpers.getFileList
import engine.file_helpers.getFolderList
import engine.file_helpers.isFile
import engine.file_helpers.isFolder
import engine.model.texture.Texture
import engine.texture_atlas.worldAtlas
import groovy.lang.Binding
import groovy.util.GroovyScriptEngine
import java.nio.file.Path
import kotlin.io.path.name

/*
api works as a state machine.
*/

object Api {

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
    Block.updateTextureCoords()
  }

  private fun loadMods() {
    getFolderList(MOD_BASE_FOLDER).unwrap().forEach { thisFolder: Path ->
      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
      currentModName = thisFolder.name
      currentModFolder = thisFolder.name
      currentDirectoryLiteral = "$MOD_BASE_FOLDER/$currentModFolder"
      if (!currentDirectoryLiteral.isFolder()) throw RuntimeException("Api: Something strange has gone wrong with loading mods.\nFolder $thisFolder does not exist.")

      //!todo: check mod.json existence here!
      //!fixme: implement config checker!!
      //!fixme: currentModName is set here!

      //! FIXME: THIS NEEDS TO BE HANDLED BETTER.

      val currentMain = "$currentDirectoryLiteral/main.groovy"
      if (!currentMain.isFile()) throw RuntimeException("Api: $currentModName does not contain a main.groovy!")
      runFile("$currentModName/main.groovy")
    }
  }

  private fun loadTextures() {
    getFolderList(MOD_BASE_FOLDER).unwrap().forEach { thisFolder: Path ->
      //? note: For now, we will assume the mod is called the folder name. In a bit: The conf will be implemented.
      currentModName = thisFolder.name
      currentModFolder = thisFolder.name
      currentDirectoryLiteral = "$MOD_BASE_FOLDER/$currentModFolder"
      if (!currentDirectoryLiteral.isFolder()) throw RuntimeException("Api: Something strange has gone wrong with loading textures.\nFolder $thisFolder does not exist.")
      loadBlockTextures()
      loadIndividualTextures()
    }

    // Finally, flush the world atlas into the GPU.
    Texture.create("worldAtlas", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())
  }

  private fun loadBlockTextures() {
    val textureDirectory = "$MOD_BASE_FOLDER/$currentModFolder/textures"

    if (!textureDirectory.isFolder()) {
      println("Api: $currentModName has no textures folder. Skipping.")
      return
    }
    val blockTextureDirectory = "$textureDirectory/blocks"
    if (!blockTextureDirectory.isFolder()) {
      println("Api: $currentModName has no block textures folder. Skipping.")
      return
    }
    getFileList(blockTextureDirectory)
      .unwrap()
      .filter { it.name.endsWith(".png") }
      .ifEmpty { println("Api: $currentModName has no block textures in folder. Skipping."); return }
      .forEach { foundTexture: Path ->
        worldAtlas.add(
          foundTexture.name,
          "$blockTextureDirectory/${foundTexture.name}"
        )
      }
  }

  private fun loadIndividualTextures() {
    val textureDirectory = "$MOD_BASE_FOLDER/$currentModFolder/textures"
    if (!textureDirectory.isFolder()) {
      println("Api: $currentModName has no textures folder. Skipping.")
      return
    }
    getFileList(textureDirectory)
      .unwrap()
      .filter { it.name.endsWith(".png") }
      .ifEmpty { println("Api: $currentModName has no textures in folder. Skipping."); return }
      .forEach { foundTexture: Path ->
        Texture.create(foundTexture.name, "$textureDirectory/${foundTexture.name}")
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
