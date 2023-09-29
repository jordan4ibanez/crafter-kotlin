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
  private val database = HashMap<String, MeshObject>()
  private val idDatabase = HashMap<Int, String>()

  // note: 3D and 2D are explicit here to make code more readable.

  fun create3D(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, textureName: String) {
    val meshObject = MeshObject(name, positions, textureCoords, indices, textureName, true)
    safePut(name, meshObject)
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
  val name: String
  val vaoID: Int
  val positionsID: Int
  val textureCoordsID: Int
  val indicesVboID: Int
  val indicesCount: Int
  var textureID: Int
  // Optionals.
//  val bones: Int
//  val colors: Int

  constructor(name: String, positions: FloatArray, textureCoords: FloatArray, indices: IntArray, textureName: String, is3D: Boolean) {

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

  private val database = HashMap<String, TextureObject>()
  private val idDatabase = HashMap<Int, String>()

  fun create(fileLocation: String) {
    val textureObject = TextureObject(fileLocation)
    safePut(fileLocation, textureObject)
    println("texture: Created texture $fileLocation at ${textureObject.id}")
  }

  fun create(name: String, fileLocation: String) {
    val textureObject = TextureObject(name, fileLocation)
    safePut(name, textureObject)
    println("texture: Created texture $name at ${textureObject.id}")
  }

  fun create(name: String, buffer: ByteBuffer, size: Vector2ic, channels: Int) {
    val textureObject = TextureObject(name, buffer, size, channels)
    safePut(name, textureObject)
    println("texture: Created texture $name at ${textureObject.id}")
  }

  fun destroy(name: String) {
    safeDestroy(name)
  }

  fun destroy(id: Int) {
    safeDestroy(id)
  }

  fun getID(name: String): Int {
    return safeGet(name).id
  }

  fun getSize(name: String): Vector2ic {
    return safeGet(name).size
  }

  fun getFloatingSize(name: String): Vector2fc {
    return safeGet(name).floatingSize
  }

  fun getChannels(name: String): Int {
    return safeGet(name).channels
  }

  fun getName(id: Int): String {
    return safeGet(id).name
  }

  fun exists(name: String): Boolean {
    return database.containsKey(name)
  }

  fun exists(id: Int): Boolean {
    return idDatabase.containsKey(id)
  }

  fun destroyAll() {
    database.forEach { (_, textureObject) ->
      // Debug info for now.
      println("texture: Destroying ${textureObject.id} | ${textureObject.name}")
      destroyTexture(textureObject.id)
    }
  }

  private fun safePut(name: String, textureObject: TextureObject) {
    if (database.containsKey(name)) throw RuntimeException("texture: Attempted to overwrite existing texture. $name")
    database[name] = textureObject
    idDatabase[textureObject.id] = name
  }

  private fun safeGet(name: String): TextureObject {
    // A handy utility to prevent unwanted behavior.
    return database[name] ?: throw RuntimeException("texture: Attempted to index nonexistent texture. $name")
  }

  private fun safeGet(id: Int): TextureObject {
    // A handy utility to prevent unwanted behavior.
    val name = idDatabase[id] ?: throw RuntimeException("texture: Attempted to index nonexistent ID. $id")
    return safeGet(name)
  }

  private fun safeDestroy(name: String) {
    // This is safe because it will error out if this texture does not exist automatically.
    val id = safeGet(name).id
    destroyTexture(id)
    database.remove(name)
    idDatabase.remove(id)
  }

  private fun safeDestroy(id: Int) {
    // This is safe because it will error out if this texture does not exist automatically.
    val name = safeGet(id).name
    destroyTexture(id)
    database.remove(name)
    idDatabase.remove(id)
  }
}

private class TextureObject {
  val name: String
  val id: Int
  val size: Vector2i = Vector2i()
  val floatingSize: Vector2f = Vector2f()
  val channels: Int

  constructor(name: String, buffer: ByteBuffer, size: Vector2ic, channels: Int) {

    // Clones a texture.

    this.name = name
    this.size.set(size)
    this.floatingSize.set(size.x().toFloat(), size.y().toFloat())
    this.channels = channels

    id = uploadTextureBuffer(name, size, buffer)

    //note: This does not destroy the buffer because the buffer could still be in use!
  }

  constructor(fileLocation: String) {

    // Creates a GL texture from a file location.

    name = fileLocation

    val (buffer, width, height, channels) = constructTextureFromFile(fileLocation)

    size.set(width,height)
    floatingSize.set(width.toFloat(),height.toFloat())
    this.channels = channels

    id = uploadTextureBuffer(name, size, buffer)

    destroyTextureBuffer(buffer)
  }

  constructor(name: String, fileLocation: String) {
    // Creates a GL texture from a file location with a custom name.

    this.name = name

    val (buffer, width, height, channels) = constructTextureFromFile(fileLocation)

    size.set(width,height)
    floatingSize.set(width.toFloat(),height.toFloat())
    this.channels = channels

    id = uploadTextureBuffer(name, size, buffer)

    destroyTextureBuffer(buffer)
  }
}

data class TextureData(val buffer: ByteBuffer, val width: Int, val height: Int, val channels: Int)

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