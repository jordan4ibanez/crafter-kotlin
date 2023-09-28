package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector4i
import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

/*
A texture packer that automates into an atlas.
This is designed very strangely because it's meant to be an internal module.
Self-contained, sleek, black box.
 */

private const val CHANNELS = 4

private class Canvas {
  var data: ByteBuffer = createByteBuffer(0)
  val size: Vector2i = Vector2i()

  constructor(width: Int, height: Int) {
    resize(width, height)
  }

  constructor(fileLocation: String) {
    val stack: MemoryStack
    try {
      stack = MemoryStack.stackPush()
    } catch (e: Exception) {
      throw RuntimeException("Canvas: Failed to allocate stack memory.")
    }

    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)
    val channels = stack.mallocInt(1)

    data = stbi_load(fileLocation, width, height, channels, 4)!!

    if (channels[0] != CHANNELS) {
      throw RuntimeException("Canvas: Attempted to upload image with ${channels[0]} channels.")
    }

    size.set(width[0], height[0])
  }

  fun resize(width: Int, height: Int) {
    size.set(width, height)
    allocate()
  }

  fun allocate() {
    data = createByteBuffer(size.x() * size.y() * CHANNELS)
  }

  fun getPixel(x: Int, y: Int): Vector4i {
    collisionDetect(x, y)
    val index = toIndex(x, y)
    //fixme: needs testing
    return Vector4i(
      data.get(index).toUByte().toInt(),
      data.get(index + 1).toUByte().toInt(),
      data.get(index + 2).toUByte().toInt(),
      data.get(index + 4).toUByte().toInt()
    )
  }

  fun setPixel(x: Int, y: Int, color: Vector4i) {
    collisionDetect(x, y)
    colorCheck(color)
    val index = toIndex(x, y)
    //fixme: needs testing
    data.put(index, color.x().toByte())
    data.put(index + 1, color.y().toByte())
    data.put(index + 2, color.z().toByte())
    data.put(index + 3, color.w().toByte())
  }

  private fun toIndex(x: Int, y: Int): Int {
    val width = size.y() * CHANNELS
    return (y * width) + (x * CHANNELS)
  }

  private fun collisionDetect(x: Int, y: Int) {
    if (x < 0 || x >= size.x() || y < 0 ||y >= size.y()) {
      throw RuntimeException("Canvas: Tried to access out of bounds. $x, $y | Limit: ${size.x()}, ${size.y()}")
    }
  }

  private fun colorCheck(color: Vector4i) {
    color.destructure().zip(arrayOf("red", "blue", "green", "alpha")).forEach {
      val (value, name) = it
      if (value < 0 ||value > 255) {
        throw RuntimeException("Canvas: $name out of bounds. $value. Limit: 0-255")
      }
    }
  }

  fun destroy() {
    stbi_image_free(data)
  }
}

