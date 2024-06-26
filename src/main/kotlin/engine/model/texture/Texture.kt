package engine.model.texture

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer


//note: Texture operations.

// texture works as a factory, container, and namespace. All in one.
object Texture {

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
//    println("texture: Created texture $fileLocation at $id")
    return id
  }

  fun create(name: String, fileLocation: String): Int {
    //? note: Returns texture ID.
    val id = internalCreate(name, fileLocation)
//    println("texture: Created texture $name at $id")
    return id
  }

  fun create(name: String, buffer: ByteBuffer, size: Vector2ic, channels: Int): Int {
    //? note: Returns texture ID.
    val id = internalCreate(name, buffer, size, channels)
//    println("texture: Created texture $name at $id")
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
    return floatingSize[getID(name)]
      ?: throw RuntimeException("texture: Tried to get non-existent floating size. $name")
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
//      println("texture: Destroying $id | ${getName(id)}")
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
    val newSize = Vector2i(width, height)
    val newFloatingSize = Vector2f(width.toFloat(), height.toFloat())
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

    val buffer: ByteBuffer = stbi_load(fileLocation, stackWidth, stackHeight, stackChannels, 4)
      ?: throw RuntimeException("STBI: Failed to load texture. $fileLocation")
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
    val borderColor = floatArrayOf(0f, 0f, 0f, 0f)

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
    glGenerateMipmap(GL_TEXTURE_2D)

    return textureID
  }

  private fun destroyTexture(id: Int) {
    glDeleteTextures(id)
  }
}