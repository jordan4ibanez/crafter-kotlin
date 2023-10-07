package engine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

//note: Mesh functions

// mesh works as a factory, container, and namespace. All in one.
object mesh {

  val id = HashMap<String, Int>()
  val name = HashMap<Int, String>()
  val positionsID = HashMap<Int, Int>()
  val textureCoordsID = HashMap<Int, Int>()
  val indicesVboID = HashMap<Int, Int>()
  val indicesCount = HashMap<Int, Int>()
  val textureID = HashMap<Int, Int>()
  //? note: Optionals.
  val colorsID = HashMap<Int, Int>()
  val bonesID = HashMap<Int, Int>()


//  // FIXME: Data orient this!
//  private val database = HashMap<String, MeshObject>()
//  private val idDatabase = HashMap<Int, String>()

  // note: 3D and 2D are explicit here to make code more readable.

  fun create3D(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, textureName: String) {
    val meshObject = MeshObject(name, positions, textureCoords, indices, textureName, true)
    safePut(name, meshObject)
  }

  fun create3D(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, light: FloatArray, textureName: String) {
    val meshObject = MeshObject(name, positions, textureCoords, indices, light, textureName, true)
  }

  fun create2D(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, textureName: String) {
    val meshObject = MeshObject(name, positions, textureCoords, indices, textureName, false)
    safePut(name, meshObject)
  }

  fun draw(name: String) {
    drawMesh(safeGet(name))
  }

  fun drawLines(name: String) {
    drawMeshLineMode(safeGet(name))
  }

  fun destroy(name: String) {
    safeDestroy(name)
  }

  fun getID(name: String): Int {
    return safeGet(name).vaoID
  }

  fun getName(vaoID: Int): String {
    return safeGet(vaoID).name
  }

  fun exists(name: String): Boolean {
    return database.containsKey(name)
  }

  fun swapTexture(name: String, newTextureName: String) {
    safeGet(name).swapTexture(newTextureName)
  }

  fun destroyAll() {
    database.forEach {(_, meshObject) ->
      // Debug info for now.
      println("mesh: Destroying ${meshObject.vaoID} | ${meshObject.name}")
      destroyMesh(meshObject)
    }
  }

  private fun safePut(name: String, meshObject: MeshObject) {
    if (database.containsKey(name)) throw RuntimeException("mesh: Attempted to overwrite existing mesh. $name")
    database[name] = meshObject
    idDatabase[meshObject.vaoID] = name
  }

  private fun safeGet(name: String): MeshObject {
    // A handy utility to prevent unwanted behavior.
    return database[name] ?: throw RuntimeException("mesh: Attempted to index nonexistent mesh. $name")
  }

  private fun safeGet(vaoID: Int): MeshObject {
    // A handy utility to prevent unwanted behavior.
    val name = idDatabase[vaoID] ?: throw RuntimeException("mesh: Attempted to index nonexistent vaoID. $vaoID")
    return safeGet(name)
  }

  private fun safeDestroy(name: String) {
    // This is safe because it will error out if this texture does not exist automatically.
    val meshObject = safeGet(name)
    destroyMesh(meshObject)
    database.remove(name)
    idDatabase.remove(meshObject.vaoID)
  }
}

//todo: this will be interesting
//private class TexturelessMesh {
//
//}

private class MeshObject {

  //! FIXME: data orient this!
  val name: String
  val vaoID: Int
  val positionsID: Int
  val textureCoordsID: Int
  val indicesVboID: Int
  val indicesCount: Int
  var textureID: Int
  // Optionals.
  val colorsID: Int
//  val bones: Int

  constructor(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, textureName: String, is3D: Boolean):
    this(name, positions, textureCoords, indices, FloatArray(0), textureName, is3D)

  constructor(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, colors: FloatArray, textureName: String, is3D: Boolean) {

    // Check texture existence before continuing.
    try {
      textureID = texture.getID(textureName)
    } catch (e: RuntimeException) {
      throw RuntimeException("Mesh: Tried to use nonexistent texture. $textureName")
    }

    this.name = name
    indicesCount = indices.size

    vaoID = glGenVertexArrays()

    // GL State machine Object assignment begin.
    glBindVertexArray(vaoID)

    // Store the width of the components. Vector3f or Vector2f, basically.
    val componentWidth = if (is3D) 3 else 2

    positionsID     = uploadFloatArray(positions, 0, componentWidth)
    textureCoordsID = uploadFloatArray(textureCoords, 1, 2)
    indicesVboID    = uploadIndices(indices)

    //optionals
    colorsID = if (colors.isNotEmpty()) uploadFloatArray(colors, 2, 4) else 0


    // Finally unbind the VAO.
    glBindVertexArray(0)
  }

  fun swapTexture(name: String) {
    val newTextureID: Int
    try {
      newTextureID = texture.getID(name)
    } catch (e: Exception) {
      throw RuntimeException("Mesh: Attempted to hotswap to nonexistent texture. $name")
    }
    textureID = newTextureID
  }
}

private fun drawMesh(meshObject: MeshObject) {
  //note: There were a few things in the Java version, see about implementing them again.

  glBindTexture(GL_TEXTURE_2D, meshObject.textureID)
  glBindVertexArray(meshObject.vaoID)
  glDrawElements(GL_TRIANGLES, meshObject.indicesCount, GL_UNSIGNED_INT, 0)

  //note: Unbinding is optional. Done for safety.
  glBindVertexArray(0)
}

private fun drawMeshLineMode(meshObject: MeshObject) {
  glBindTexture(GL_TEXTURE_2D, meshObject.textureID)
  glBindVertexArray(meshObject.vaoID)
  glDrawElements(GL_LINES, meshObject.indicesCount, GL_UNSIGNED_INT, 0)

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

private fun destroyMesh(meshObject: MeshObject) {

  glBindVertexArray(meshObject.vaoID)

  destroyVBO(meshObject.positionsID, 0, "positions")
  destroyVBO(meshObject.textureCoordsID, 1, "texture coords")
  destroyVBO(meshObject.colorsID, 2, "colors")
  destroyVBO(meshObject.indicesVboID, -1, "indices")

  // Todo: destroy the bones and colors VBO

  // Now unbind.
  glBindVertexArray(0)

  // Then destroy the VBO.
  glDeleteVertexArrays(meshObject.vaoID)
  if (glIsVertexArray(meshObject.vaoID)) {
    throw RuntimeException("destroyMesh: Failed to destroy VAO ${meshObject.vaoID} | ${meshObject.name}")
  }
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


//note: Texture operations.

// texture works as a factory, container, and namespace. All in one.
object texture {

  //note: For now textures in the game remain until game is closed.
  // But, in the future we will want textures to be able to be cleared from GL memory.
  // This is designed for that.

  //note: Functional, data-oriented.

  @JvmRecord
  private data class TextureData(val buffer: ByteBuffer, val width: Int, val height: Int, val channels: Int)


  private val id = HashMap<String, Int>()
  private val name = HashMap<Int, String>()
  private val size = HashMap<Int, Vector2ic>()
  private val floatingSize = HashMap<Int, Vector2fc>()
  private val channels = HashMap<Int, Int>()

  fun create(fileLocation: String): Int {
    //? note: Returns texture ID.
    val id = internalCreate(fileLocation)
    println("texture: Created texture $fileLocation at $id")
    return id
  }

  fun create(name: String, fileLocation: String): Int {
    //? note: Returns texture ID.
    val id = internalCreate(name, fileLocation)
    println("texture: Created texture $name at $id")
    return id
  }

  fun create(name: String, buffer: ByteBuffer, size: Vector2ic, channels: Int): Int {
    //? note: Returns texture ID.
    val id = internalCreate(name, buffer, size, channels)
    println("texture: Created texture $name at $id")
    return id
  }

  fun exists(id: Int): Boolean {
    return name.containsKey(id)
  }
  fun exists(name: String): Boolean {
    return id.containsKey(name)
  }

  fun destroy(id: Int) {
    safeDestroy(id)
  }
  fun destroy(name: String) {
    safeDestroy(getID(name))
  }

  fun getID(name: String): Int {
    return id[name] ?: throw RuntimeException("texture: Tried to get non-existent ID. $name")
  }
  fun getName(id: Int): String {
    return name[id] ?: throw RuntimeException("texture: Tried to get non-existent name. $id")
  }


  fun getSize(id: Int): Vector2ic {
    return size[id] ?: throw RuntimeException("texture: Tried to get non-existent size. $id")
  }
  fun getSize(name: String): Vector2ic {
    return size[getID(name)] ?: throw RuntimeException("texture: Tried to get non-existent size. $name")
  }

  fun getFloatingSize(id: Int): Vector2fc {
    return floatingSize[id] ?: throw RuntimeException("texture: Tried to get non-existent floating size. $id")
  }
  fun getFloatingSize(name: String): Vector2fc {
    return floatingSize[getID(name)] ?: throw RuntimeException("texture: Tried to get non-existent floating size. $name")
  }

  fun getChannels(id: Int): Int {
    return channels[id] ?: throw RuntimeException("texture: Tried to get non-existent channels. $id")
  }
  fun getChannels(name: String): Int {
    return channels[getID(name)] ?: throw RuntimeException("texture: Tried to get non-existent channels. $name")
  }

  fun destroyAll() {
    //? note: This is for end of program life, but could also be used for texture packs.
    name.keys.forEach { id ->
      // Debug info for now.
      println("texture: Destroying $id | ${getName(id)}")
      destroyTexture(id)
    }
    name.clear()
    size.clear()
    floatingSize.clear()
    channels.clear()
  }

  //? note: Begin internal complex API elements.

  private fun internalCreate(newName: String, buffer: ByteBuffer, originalSize: Vector2ic, newChannels: Int): Int {
    checkDuplicate(newName)
    //? note: Returns texture ID.
    // Clones a texture.
    val newSize = Vector2i(originalSize)
    val newFloatingSize = Vector2f(newSize.x().toFloat(), newSize.y().toFloat())
    val newID = uploadTextureBuffer(newName, newSize, buffer)
    //? note: This does not destroy the buffer because the buffer could still be in use!
    // All required data has been created. Store.
    put(newID, newName, newSize, newFloatingSize, newChannels)
    return newID
  }

  private fun internalCreate(fileLocation: String): Int {
    checkDuplicate(fileLocation)
    //? note: Returns texture ID.
    // Creates a GL texture from a file location.
    val (buffer, width, height, newChannels) = constructTextureFromFile(fileLocation)
    val newSize = Vector2i(width, height)
    val newFloatingSize = Vector2f(width.toFloat(), height.toFloat())
    val newID = uploadTextureBuffer(fileLocation, newSize, buffer)
    destroyTextureBuffer(buffer)
    // All required data has been created. Store.
    put(newID, fileLocation, newSize, newFloatingSize, newChannels)
    return newID
  }

  private fun internalCreate(newName: String, fileLocation: String): Int {
    checkDuplicate(newName)
    //? note: Returns texture ID.
    // Creates a GL texture from a file location with a custom name.
    val (buffer, width, height, newChannels) = constructTextureFromFile(fileLocation)
    val newSize = Vector2i(width,height)
    val newFloatingSize = Vector2f(width.toFloat(),height.toFloat())
    val newID = uploadTextureBuffer(newName, newSize, buffer)
    destroyTextureBuffer(buffer)
    // All required data has been created. Store.
    put(newID, newName, newSize, newFloatingSize, newChannels)
    return newID
  }

  private fun put(newID: Int, newName: String, newSize: Vector2ic, newFloatingSize: Vector2fc, newChannels: Int) {
    id[newName] = newID
    name[newID] = newName
    size[newID] = newSize
    floatingSize[newID] = newFloatingSize
    channels[newID] = newChannels
  }

  private fun safeDestroy(newID: Int) {
    // This is safe because it will error out if this texture does not exist automatically.
    destroyTexture(newID)
    val gottenName = name[newID] ?: throw RuntimeException("texture: Tried to destroy non-existent ID, $newID")
    id.remove(gottenName)
    name.remove(newID)
    size.remove(newID)
    floatingSize.remove(newID)
    channels.remove(newID)
  }

  private fun checkDuplicate(name: String) {
    if (id.contains(name)) throw RuntimeException("texture: Attempted to store duplicate of $name")
  }
  private fun checkDuplicate(id: Int) {
    if (name.containsKey(id)) throw RuntimeException("texture: Attempted to store duplicate of $id")
  }

  private fun constructTextureFromFile(fileLocation: String): TextureData {

    val stack: MemoryStack = try {
      MemoryStack.stackPush()
    } catch (e: RuntimeException) {
      throw RuntimeException("constructTextureFromFile: Failed to push memory stack.")
    }
    val stackWidth: IntBuffer = stack.mallocInt(1)
    val stackHeight: IntBuffer = stack.mallocInt(1)
    val stackChannels: IntBuffer = stack.mallocInt(1)

    val buffer: ByteBuffer = stbi_load(fileLocation, stackWidth, stackHeight, stackChannels, 4) ?: throw RuntimeException("STBI: Failed to load texture. $fileLocation")
    val width = stackWidth.get(0)
    val height = stackWidth.get(0)
    val channels = stackChannels.get(0)
    return TextureData(buffer, width, height, channels)
  }

  // This makes the STBI call very explicit.
  private fun destroyTextureBuffer(buffer: ByteBuffer) {
    stbi_image_free(buffer)
  }

  private fun uploadTextureBuffer(name: String, size: Vector2ic, buffer: ByteBuffer): Int {

    val textureID = glGenTextures();

    glBindTexture(GL_TEXTURE_2D, textureID);

    // Enable texture clamping to edge
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

    // Border color is nothing - This is a GL REQUIRED float
    val borderColor = floatArrayOf(0f,0f,0f,0f)

    glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);

    // Add in nearest neighbor texture filtering
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x(), size.y(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

    // If this gets called, the driver most likely has an issue.
    if (!glIsTexture(textureID)) {
      throw RuntimeException("Texture: OpenGL failed to upload $name into GPU memory!")
    }
    glGenerateMipmap(GL_TEXTURE_2D);

    return textureID
  }

  private fun destroyTexture(id: Int) {
    glDeleteTextures(id)
  }
}