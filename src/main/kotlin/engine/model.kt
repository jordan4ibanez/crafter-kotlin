package engine

import org.joml.Vector2f
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

//Note: inverseDatabase
// The existence check inverse database. Can quickly check if the database contains a texture by ID.
// Do not remove this, this is causing O(1) performance penalty. Safer to have this.

// Mesh works as a factory, container, and namespace. All in one.
object mesh {


}

object texture {
  private val database = HashMap<String, Texture>()
  private val inverseDatabase = HashMap<Int, String>()

  fun createTexture(fileLocation: String) {
    database[fileLocation] = Texture(fileLocation)
  }

  fun destroyTexture(name: String) {
    // This is safe because it will error out if this texture does not exist automatically.
    destroyTexture(safeGet(name).id)
    database.remove(name)
  }

  fun getID(name: String): Int {
    return safeGet(name).id
  }

  private fun safePut(name: String, textureObject: Texture) {
    if (database.containsKey(name)) throw RuntimeException("texture: Tried to overwrite existing texture. $name")
  }

  private fun safeGet(name: String): Texture {
    // A handy utility to prevent unwanted behavior.
    return database[name] ?: throw RuntimeException("texture: Attempted to index nonexistent texture. $name")
  }
}

private class Texture {
  val name: String
  val id: Int
  val size: Vector2i = Vector2i()
  val floatingSize: Vector2f = Vector2f()
  val channels: Int

  //TODO: This needs to be implemented later.
  // This clones a Texture.
//  constructor(name: String, buffer: ByteBuffer, size: Vector2ic) {
//    val stack: MemoryStack = try {
//      MemoryStack.stackPush()
//    } catch (e: RuntimeException) {
//      throw RuntimeException("Texture: Failed to push memory stack.")
//    }
//  }

  constructor(fileLocation: String) {
    name = fileLocation

    val (buffer, width, height, channels) = constructTextureFromFile(fileLocation)

    size.set(width,height)
    floatingSize.set(width.toFloat(),height.toFloat())
    this.channels = channels

    id = bufferToGL(name, size, buffer)

    destroyTextureBuffer(buffer)
  }
}

private class Mesh {

}

//todo: this will be interesting
//private class TexturelessMesh {
//
//}

//note: Mesh functions


//note: Texture functions

@JvmRecord
data class TextureData(val buffer: ByteBuffer, val width: Int, val height: Int, val channels: Int)

private fun constructTextureFromFile(fileLocation: String): TextureData {

  val stack: MemoryStack = try {
    MemoryStack.stackPush()
  } catch (e: RuntimeException) {
    throw RuntimeException("Texture: Failed to push memory stack.")
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