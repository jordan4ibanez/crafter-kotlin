package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector4f
import org.joml.Vector4fc
import org.joml.Vector4i
import org.joml.Vector4ic
import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import org.lwjgl.stb.STBImageWrite.stbi_write_png
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.util.SortedSet
import java.util.TreeSet
import java.util.UUID.randomUUID



val worldAtlas = Packer("worldAtlas")
val fontAtlas = Packer("fontAtlas")

// note: Everything below this is just implementation.
//  You only really talk to the two vals above.


/*
A texture packer that automates into an atlas.
This is designed very strangely because it's meant to be an internal module.
Self-contained, sleek, black box.
 */

private const val CHANNELS = 4
private const val UNDER_PROVISION = 0.00001f;

private class Canvas {
  val uuid: String
  val position = Vector2i()
  var data: ByteBuffer = createByteBuffer(0)
  val size: Vector2i = Vector2i()
  var packed = false

  constructor(width: Int, height: Int) {
    resize(width, height)
    uuid = randomUUID().toString()
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

    uuid = randomUUID().toString()
  }

  fun resize(width: Int, height: Int) {
    size.set(width, height)
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
      data.get(index + 3).toUByte().toInt()
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

  fun setPosition(x: Int, y: Int) {
    position.set(x, y)
  }

  fun getPositionAndSize(): Vector4i {
    return Vector4i(position.x(), position.y(), size.x(), size.y())
  }

  private fun toIndex(x: Int, y: Int): Int {
    val width = size.x() * CHANNELS
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

class Packer {
  private val padding = 1
  private val edgeColor = Vector4i(255,0,0,255)
  private val blankSpaceColor = Vector4i(0,0,0,0)
  private val showDebugEdge = false
  private val expansionAmount = 16
  private val size = Vector2i(16, 16)
  private var maxSize = Vector2i(0,0)
  private val textures = HashMap<String, Canvas>()
  private val canvas = Canvas(size.x(), size.y())
  private val availableX: SortedSet<Int> = TreeSet()
  private val availableY: SortedSet<Int> = TreeSet()
  private var locked = false
  private val name: String

  constructor(name: String) {
    availableX.add(padding)
    availableY.add(padding)
    this.name = name
  }

  fun isEmpty(): Boolean {
    return textures.isEmpty()
  }

  fun fileNameExists(fileName: String): Boolean {
    return textures.containsKey(fileName)
  }

  fun getIntegralPosition(fileName: String): Vector4ic {
    return textures[fileName]!!.getPositionAndSize()
  }

  fun getOpenGLPosition(fileName: String): Vector4fc {
    val gotten = getIntegralPosition(fileName)
    return Vector4f(
      (gotten.x().toFloat() / maxSize.x().toFloat()) + UNDER_PROVISION,
      (gotten.y().toFloat() / maxSize.y().toFloat()) + UNDER_PROVISION,
      (gotten.z().toFloat() / maxSize.x().toFloat()) - (UNDER_PROVISION * 2f),
      (gotten.w().toFloat() / maxSize.y().toFloat()) - (UNDER_PROVISION * 2f)
    )
  }

  fun getQuadOf(fileName: String): FloatArray {
    val p = getOpenGLPosition(fileName)
    return floatArrayOf(
      p.x(), p.y(),
      p.x(), p.y() + p.w(),
      p.x() + p.z(), p.y() + p.w(),
      p.x() + p.z(), p.y()
    )
  }

  fun getQuadOf(fileName: String, xLeftTrim: Float, xRightTrim: Float, yTopTrim: Float, yBottomTrim: Float): FloatArray {
    arrayOf("xLeftTrim", "xRightTrim", "yTopTrim", "yBottomTrim").zip(arrayOf(xLeftTrim, xRightTrim, yTopTrim, yBottomTrim)).forEach {
      val (name, value) = it
      if (value > 1 || value < 0) throw RuntimeException("Packer: Value for $name out of bounds. 0-1. Got: $value")
    }

    val o = getOpenGLPosition(fileName)

    val adjustedXLeftTrim = (xLeftTrim * o.z()) + o.x();
    val adjustedXRightTrim = (xRightTrim * o.z()) + o.x() + o.z();
    val adjustedYTopTrim = (yTopTrim * o.w()) + o.y();
    val adjustedYBottomTrim = (yBottomTrim * o.w()) + o.y() + o.w();

    return floatArrayOf(
      adjustedXLeftTrim,   adjustedYTopTrim,
      adjustedXLeftTrim,   adjustedYBottomTrim,
      adjustedXRightTrim , adjustedYBottomTrim,
      adjustedXRightTrim,  adjustedYTopTrim
    )
  }

  fun add(name: String, fileLocation: String) {
    if (textures.containsKey(name)) throw RuntimeException("Packer: tried to add duplicate of $name")
    if (textures.containsKey(fileLocation)) throw RuntimeException("Packer: tried to add duplicate of $fileLocation")
    textures[name] = Canvas(fileLocation)
    println("Packer[${this.name}]: added $fileLocation as $name")
  }

  fun debugPrintCanvas() {
    pack()
    stbi_write_png("test.png", canvas.size.x(), canvas.size.y(), CHANNELS, canvas.data, canvas.size.x() * 4)
  }

  fun flush(): ByteBuffer {
    pack()
    return canvas.data
  }

  private fun pack() {
    textures.values.forEach { textureCanvas ->
      while(!tetrisPack(textureCanvas)) {
        val currentSize = canvas.size
        canvas.resize(currentSize.x + expansionAmount, currentSize.y + expansionAmount)
      }
    }
    flushCanvas()
  }

  private fun flushCanvas() {
    canvas.resize(maxSize.x, maxSize.y)
    canvas.allocate()

    textures.forEach { pair ->
      val (name, textureCanvas) = pair

      val posX = textureCanvas.position.x
      val posY = textureCanvas.position.y

      val sizeX = textureCanvas.size.x
      val sizeY = textureCanvas.size.y

      textureCanvas.size.print("$name | $posX | $posY")

      for (x in 0 until sizeX) {
        println(x + posX)
        for (y in 0 until sizeY) {
//          println("$x, $y")
          val color = textureCanvas.getPixel(x,y)
          canvas.setPixel(x + posX, y + posY, color)
        }
      }
    }

    textures.values.forEach {textureCanvas ->
      textureCanvas.destroy()
    }
  }

  private fun tetrisPack(textureCanvas: Canvas): Boolean {
    var found = false

    val maxX = canvas.size.x
    val maxY = canvas.size.y

    val thisWidth = textureCanvas.size.x
    val thisHeight = textureCanvas.size.y

    var bestX = padding
    var bestY = padding


    run exit@ {
      availableY.forEach yLoop@{ hmm ->
        val y = hmm!!

        if (found) return@exit

        availableX.forEach xLoop@{ hmm2 ->
          val x = hmm2!!

          if (x + thisWidth + padding >= maxX || y + thisHeight + padding >= maxY) return@yLoop

          var failed = false

          textures.values.forEach nextObject@ { other ->
            if (!other.packed) return@nextObject
            if (other.uuid == textureCanvas.uuid) return@nextObject

            val otherX = other.position.x
            val otherY = other.position.y

            val otherWidth = other.size.x
            val otherHeight = other.size.y

            if (otherX + otherWidth + padding > x &&
              otherX <= x + thisWidth + padding &&
              otherY + otherHeight + padding > y &&
              otherY <= y + thisHeight + padding) {

              failed = true
              return@xLoop
            }
          }

          if (!failed) {
            found = true
            bestX = x
            bestY = y
            return@yLoop
          }
        }
      }
    }

    if (!found) return false

    textureCanvas.setPosition(bestX, bestY)
    textureCanvas.packed = true

    val spotRight = bestX + thisWidth + padding
    val spotBelow = bestY + thisHeight + padding

    availableX.add(spotRight)
    availableY.add(spotBelow)

    var curX = maxSize.x()
    var curY = maxSize.y()
    if (spotRight > maxSize.x()) {
      curX = spotRight
    }
    if (spotBelow > maxSize.y()) {
      curY = spotBelow
    }
    maxSize.set(curX, curY)
    return true
  }

  fun getSize(): Vector2ic {
    return canvas.size
  }


}