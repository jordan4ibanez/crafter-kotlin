package engine.model.mesh

import engine.model.texture.Texture
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

//note: Mesh functions

// mesh works as a component system and namespace.

object Mesh {


  private val id = HashMap<String, Int>()
  private val name = HashMap<Int, String>()
  private val positionsID = HashMap<Int, Int>()
  private val textureCoordsID = HashMap<Int, Int>()
  private val indicesVboID = HashMap<Int, Int>()
  private val indicesCount = HashMap<Int, Int>()
  private val textureID = HashMap<Int, Int>()

  //? note: Optionals.
  private val colorsID = HashMap<Int, Int>()
  private val bonesID = HashMap<Int, Int>()

  // note: 3D and 2D are explicit here to make code more readable.

  fun create3D(
    name: String,
    positions: FloatArray,
    textureCoords: FloatArray,
    indices: IntArray,
    textureName: String
  ): Int =
    internalCreate(name, positions, textureCoords, indices, textureName, true)

  fun create3D(
    name: String,
    positions: FloatArray,
    textureCoords: FloatArray,
    indices: IntArray,
    light: FloatArray,
    textureName: String
  ): Int =
    internalCreate(name, positions, textureCoords, indices, light, textureName, true)

  fun create2D(
    name: String,
    positions: FloatArray,
    textureCoords: FloatArray,
    indices: IntArray,
    textureName: String
  ): Int =
    internalCreate(name, positions, textureCoords, indices, textureName, false)

  private fun internalCreate(
    name: String,
    positions: FloatArray,
    textureCoords: FloatArray,
    indices: IntArray,
    textureName: String,
    is3D: Boolean
  ): Int =
    internalCreate(name, positions, textureCoords, indices, FloatArray(0), textureName, is3D)

  private fun internalCreate(
    newName: String,
    positions: FloatArray,
    textureCoords: FloatArray,
    indices: IntArray,
    colors: FloatArray,
    textureName: String,
    is3D: Boolean
  ): Int {
    val newID: Int
    val newPositionsID: Int
    val newTextureCoordsID: Int
    val newIndicesVboID: Int
    val newIndicesCount: Int = indices.size
    val newTextureID: Int
    //? note: Optionals.
    val newColorsID: Int
//    val newBonesID: Int
    // Check texture existence before continuing.
    try {
      newTextureID = Texture.getID(textureName)
    } catch (e: RuntimeException) {
      throw RuntimeException("mesh: Tried to use nonexistent texture. $textureName")
    }
    newID = glGenVertexArrays()
    // GL State machine Object assignment begin.
    glBindVertexArray(newID)
    // Store the width of the components. Vector3f or Vector2f, basically.
    val componentWidth = if (is3D) 3 else 2
    // Required.
    newPositionsID = uploadFloatArray(positions, 0, componentWidth)
    newTextureCoordsID = uploadFloatArray(textureCoords, 1, 2)
    newIndicesVboID = uploadIndices(indices)
    // Optionals.
    newColorsID = if (colors.isNotEmpty()) uploadFloatArray(colors, 2, 4) else 0
    // All required data has been created. Store.
    id[newName] = newID
    name[newID] = newName
    positionsID[newID] = newPositionsID
    textureCoordsID[newID] = newTextureCoordsID
    indicesVboID[newID] = newIndicesVboID
    indicesCount[newID] = newIndicesCount
    textureID[newID] = newTextureID
    // All optional data has been created. Store.
    if (newColorsID != 0) colorsID[newID] = newColorsID
    //todo: bones go here.
    // Finally unbind the VAO.
    glBindVertexArray(0)
    return newID
  }

  fun draw(id: Int) {
    drawMesh(id)
  }

  fun draw(name: String) {
    drawMesh(getID(name))
  }

  fun drawLines(id: Int) {
    drawMeshLineMode(id)
  }

  fun drawLines(name: String) {
    drawMeshLineMode(getID(name))
  }

  fun destroy(id: Int) {
    try {
      destroyMesh(id)
    } catch (e: Exception) {
      throw RuntimeException("mesh: Tried to destroy non-existent mesh. $id\n$e")
    }
  }

  fun destroy(name: String) {
    try {
      destroyMesh(getID(name))
    } catch (e: Exception) {
      throw RuntimeException("mesh: Tried to destroy non-existent mesh. $name\n$e")
    }
  }

  fun exists(id: Int): Boolean {
    return name.containsKey(id)
  }

  fun exists(name: String): Boolean {
    return id.containsKey(name)
  }

  fun getID(name: String): Int {
    return id[name] ?: throwNonExistent("ID", name)
  }

  fun getName(id: Int): String {
    return name[id] ?: throw RuntimeException("mesh: Tried to get non-existent name. $id")
  }

  fun getPositionsID(id: Int): Int {
    return positionsID[id] ?: throwNonExistent("positions", id)
  }

  fun getPositionsID(name: String): Int {
    return positionsID[getID(name)] ?: throwNonExistent("positions", name)
  }

  fun getTextureCoordsID(id: Int): Int {
    return textureCoordsID[id] ?: throwNonExistent("texture coords", id)
  }

  fun getTextureCoordsID(name: String): Int {
    return textureCoordsID[getID(name)] ?: throwNonExistent("texture coords", name)
  }

  fun getIndicesVboID(id: Int): Int {
    return indicesVboID[id] ?: throwNonExistent("indices VBO", id)
  }

  fun getIndicesVboID(name: String): Int {
    return indicesVboID[getID(name)] ?: throwNonExistent("indices VBO", name)
  }

  fun getIndicesCount(id: Int): Int {
    return indicesCount[id] ?: throwNonExistent("indices count", id)
  }

  fun getIndicesCount(name: String): Int {
    return indicesCount[getID(name)] ?: throwNonExistent("indices count", name)
  }

  fun getTextureID(id: Int): Int {
    return textureID[id] ?: throwNonExistent("texture ID", id)
  }

  fun getTextureID(name: String): Int {
    return textureID[getID(name)] ?: throwNonExistent("texture ID", name)
  }

  fun colorsExist(id: Int): Boolean {
    return colorsID.containsKey(id)
  }

  fun colorsExist(name: String): Boolean {
    return colorsID.containsKey(getID(name))
  }

  fun getColorsID(id: Int): Int {
    return colorsID[id] ?: throwNonExistent("colors ID", id)
  }

  fun getColorsID(name: String): Int {
    return colorsID[getID(name)] ?: throwNonExistent("colors ID", name)
  }

  fun bonesExist(id: Int): Boolean {
    return bonesID.containsKey(id)
  }

  fun bonesExist(name: String): Boolean {
    return bonesID.containsKey(getID(name))
  }

  fun getBonesID(id: Int): Int {
    return bonesID[id] ?: throwNonExistent("bones ID", id)
  }

  fun getBonesID(name: String): Int {
    return bonesID[getID(name)] ?: throwNonExistent("bones ID", name)
  }

//  fun getBonesID(id: Int): Int {
//    return bonesID
//  }

  fun swapTexture(id: Int, newTextureName: String) {
    textureID[id] = Texture.getID(newTextureName)
  }

  fun swapTexture(name: String, newTextureName: String) =
    swapTexture(getID(name), newTextureName)

  fun destroyAll() {
    //? Note: This avoid a concurrent modification exception. We have to collect IDs, then modify the container.
    val collector = ArrayList<Int>()
    id.values.forEach { collector.add(it) }
    collector.forEach { gottenID: Int ->
      // Debug info for now.
//      println("mesh: Destroying $gottenID | ${getName(gottenID)}")
      destroyMesh(gottenID)
    }
  }

  private fun drawMesh(newID: Int) {
    //note: There were a few things in the Java version, see about implementing them again.
    glBindTexture(GL_TEXTURE_2D, getTextureID(newID))
    glBindVertexArray(newID)
    glDrawElements(GL_TRIANGLES, getIndicesCount(newID), GL_UNSIGNED_INT, 0)
    //note: Unbinding is optional. Done for safety.
    glBindVertexArray(0)
  }

  private fun drawMeshLineMode(newID: Int) {
    glBindTexture(GL_TEXTURE_2D, getTextureID(newID))
    glBindVertexArray(newID)
    glDrawElements(GL_LINES, getIndicesCount(newID), GL_UNSIGNED_INT, 0)
    //note: Unbinding is optional. Done for safety.
    glBindVertexArray(0)
  }

  private fun uploadFloatArray(floatArray: FloatArray, glslPosition: Int, componentWidth: Int): Int {
    // OpenGL is a state machine. Uploading float array to current VAO state.
    // glslPosition: The "(location = X)" in the fragment shader.
    // componentWidth: 2 = Vec2, 3 = Vec3, etc
    // Returns the VBO ID.
    lateinit var buffer: FloatBuffer
    val newID: Int
    try {
      buffer = memAllocFloat(floatArray.size)
      buffer.put(floatArray).flip()
      newID = glGenBuffers()
      // Bind, push, and set pointer
      glBindBuffer(GL_ARRAY_BUFFER, newID)
      glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
      // Not normalized (false), no stride (0), pointer index (0).
      glVertexAttribPointer(glslPosition, componentWidth, GL_FLOAT, false, 0, 0)
      // Enable the GLSL array.
      glEnableVertexAttribArray(glslPosition)
      // Unbind the array buffer.
      glBindBuffer(GL_ARRAY_BUFFER, 0)
    } catch (e: Exception) {
      throw RuntimeException("uploadFloatArray: Failed to upload. $e")
    } finally {
      // Free to C float* (float[]) or else there will be a massive memory leak.
      memFree(buffer)
    }
    return newID
  }

  private fun uploadIntArray(intArray: IntArray, glslPosition: Int, componentWidth: Int): Int {
    // OpenGL is a state machine. Uploading int array to current VAO state.
    // glslPosition: The "(location = X)" in the fragment shader.
    // componentWidth: 2 = Vec2, 3 = Vec3, etc
    // Returns the VBO ID.
    lateinit var buffer: IntBuffer
    val newID: Int
    try {
      buffer = memAllocInt(intArray.size)
      buffer.put(intArray).flip()
      newID = glGenBuffers()
      // Bind, push, and set pointer
      glBindBuffer(GL_ARRAY_BUFFER, newID)
      glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
      // Not normalized (false), no stride (0), pointer index (0).
      glVertexAttribIPointer(glslPosition, componentWidth, GL_INT, 0, 0)
      // Enable the GLSL array.
      glEnableVertexAttribArray(glslPosition)
      // Unbind the array buffer.
      glBindBuffer(GL_ARRAY_BUFFER, 0)
    } catch (e: Exception) {
      throw RuntimeException("uploadIntArray: Failed to upload. $e")
    } finally {
      // Free to C int* (int[]) or else there will be a massive memory leak.
      memFree(buffer)
    }
    return newID
  }

  private fun uploadIndices(indicesArray: IntArray): Int {
    // OpenGL is a state machine. Uploading raw indices array to VAO state.
    // Returns the indices ID.
    lateinit var buffer: IntBuffer
    val newID: Int
    try {
      newID = glGenBuffers()
      buffer = memAllocInt(indicesArray.size)
      buffer.put(indicesArray).flip()
      // Not normalized (false), no stride (0), pointer index (0).
      glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, newID)
      glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
      // note: Do not unbind GL_ELEMENT_ARRAY_BUFFER
    } catch (e: Exception) {
      throw RuntimeException("uploadIndices: Failed to upload. $e")
    } finally {
      // Free to C int* (int[]) or else there will be a massive memory leak.
      memFree(buffer)
    }
    return newID
  }

  private fun destroyMesh(newID: Int) {
    val newPositionsID = getPositionsID(newID)
    val newTextureCoordsID = getTextureCoordsID(newID)
    val newIndicesVboID = getIndicesVboID(newID)
    val hasColors = colorsExist(newID)
    val newColorsID = if (hasColors) getColorsID(newID) else 0
    val hasBones = bonesExist(newID)
    val newBonesID = if (hasBones) getBonesID(newID) else 0
    glBindVertexArray(newID)
    destroyVBO(newPositionsID, 0, "positions")
    destroyVBO(newTextureCoordsID, 1, "texture coords")
    destroyVBO(newColorsID, 2, "colors")
    // Todo: destroy the bones
    destroyVBO(newIndicesVboID, -1, "indices")
    // Now unbind.
    glBindVertexArray(0)
    // Then destroy the VAO.
    glDeleteVertexArrays(newID)
    if (glIsVertexArray(newID)) {
      throw RuntimeException("destroyMesh: Failed to destroy VAO $newID | ${getName(newID)}")
    }
    // GPU memory is clear. Delete CPU memory.
    val newName = getName(newID)
//    println("mesh: destroying $newName")
    id.remove(newName)
    name.remove(newID)
    positionsID.remove(newID)
    textureCoordsID.remove(newID)
    indicesVboID.remove(newID)
    indicesCount.remove(newID)
    textureID.remove(newID)
    if (hasColors) colorsID.remove(newID)
    if (hasBones) bonesID.remove(newID)
  }

  private fun destroyVBO(vboID: Int, glslPosition: Int, vboName: String) {
    // This is used so that the indices array doesn't cause problems.
    if (glslPosition >= 0) {
      glDisableVertexAttribArray(glslPosition)
    }
    glDeleteBuffers(vboID)
    if (glIsBuffer(vboID)) {
      // Debug info for now.
      throw RuntimeException("destroyVBO: Failed to destroy vbo. $vboID | $vboName")
    }
  }

  private fun throwNonExistent(thing: String, name: String): Int {
    throw RuntimeException("mesh: Tried to get non-existent $thing. $name")
    return -1
  }

  private fun throwNonExistent(thing: String, id: Int): Int {
    throw RuntimeException("mesh: Tried to get non-existent $thing. $id")
    return -1
  }
}