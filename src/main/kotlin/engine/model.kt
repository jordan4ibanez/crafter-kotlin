package engine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

//note: Mesh functions

// mesh works as a factory, container, and namespace. All in one.
object mesh {


}

//todo: this will be interesting
//private class TexturelessMesh {
//
//}

private class Mesh {
  val name: String
  val vaoID: Int
  val positionsID: Int
  val textureCoordsID: Int
  val indicesVboID: Int
  val indicesCountID: Int
  val textureID: Int
  // Optionals.
//  val bones: Int
//  val colors: Int

  constructor(name: String, positions: FloatArray, textureCoords: FloatArray, indices: FloatArray, textureName: String) {

    // Check texture existence before continuing.
    try {
      textureID = texture.getID(textureName)
    } catch (e: RuntimeException) {
      throw RuntimeException("Mesh: Tried to use nonexistent texture. $textureName")
    }


  }
}


//note: Texture operations.

// texture works as a factory, container, and namespace. All in one.
object texture {

  //note: For now textures in the game remain until game is closed.
  // But, in the future we will want textures to be able to be cleared from GL memory.
  // This is designed for that.

  private val database = HashMap<String, Texture>()
  private val idDatabase = HashMap<Int, String>()

  fun createTexture(fileLocation: String) {
    val textureObject = Texture(fileLocation)
    safePut(fileLocation, textureObject)
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
    return idDatabase[id] ?: throw RuntimeException("texture: Attempted to index nonexistent ID. $id")
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

  private fun safePut(name: String, textureObject: Texture) {
    if (database.containsKey(name)) throw RuntimeException("texture: Attempted to overwrite existing texture. $name")
    database[name] = textureObject
    idDatabase[textureObject.id] = name
  }

  private fun safeGet(name: String): Texture {
    // A handy utility to prevent unwanted behavior.
    return database[name] ?: throw RuntimeException("texture: Attempted to index nonexistent texture. $name")
  }

  private fun safeGet(id: Int): Texture {
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

private class Texture {
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

    id = bufferToGL(name, size, buffer)

    //note: This does not destroy the buffer because the buffer could still be in use!
  }

  constructor(fileLocation: String) {

    // Creates a GL texture from a file location.

    name = fileLocation

    val (buffer, width, height, channels) = constructTextureFromFile(fileLocation)

    size.set(width,height)
    floatingSize.set(width.toFloat(),height.toFloat())
    this.channels = channels

    id = bufferToGL(name, size, buffer)

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

fun bufferToGL(name: String, size: Vector2ic, buffer: ByteBuffer): Int {

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

fun destroyTexture(id: Int) {
  glDeleteTextures(id)
}