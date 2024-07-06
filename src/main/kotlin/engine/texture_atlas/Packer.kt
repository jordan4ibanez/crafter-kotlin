package engine.texture_atlas

import org.joml.*
import org.lwjgl.stb.STBImageWrite.stbi_write_png
import java.nio.ByteBuffer
import java.util.*


/*
A texture packer that automates into an atlas.
This is designed very strangely because it's meant to be an internal module.
Self-contained, sleek, black box.
 */
class Packer {

  private val padding = 1
  private val edgeColor = Vector4i(255, 0, 0, 255)
  private val blankSpaceColor = Vector4i(0, 0, 0, 0)
  private val showDebugEdge = false
  private val expansionAmount = 16
  private val size = Vector2i(16, 16)
  private var maxSize = Vector2i(0, 0)
  private val textures = HashMap<String, Canvas>()
  private val canvas: Canvas
  private val availableX: SortedSet<Int> = TreeSet()
  private val availableY: SortedSet<Int> = TreeSet()
  private var locked = false
  private val name: String

  constructor(name: String) {
    availableX.add(padding)
    availableY.add(padding)
    this.name = name
    canvas = Canvas(name, size.x(), size.y())
  }

  fun isEmpty(): Boolean {
    return textures.isEmpty()
  }

  fun fileNameExists(fileName: String): Boolean {
    return textures.containsKey(fileName)
  }

  fun getIntegralPosition(fileName: String): Vector4ic {
    val gotten = textures[fileName] ?: throw RuntimeException("textureAtlas: $fileName does not exist.")
    return gotten.getPositionAndSize()
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

  fun getQuadOf(
    fileName: String,
    xLeftTrim: Float,
    xRightTrim: Float,
    yTopTrim: Float,
    yBottomTrim: Float
  ): FloatArray {
    arrayOf("xLeftTrim", "xRightTrim", "yTopTrim", "yBottomTrim").zip(
      arrayOf(
        xLeftTrim,
        xRightTrim,
        yTopTrim,
        yBottomTrim
      )
    ).forEach {
      val (name, value) = it
      if (value > 1 || value < 0) throw RuntimeException("Packer: Value for $name out of bounds. 0-1. Got: $value")
    }

    val o = getOpenGLPosition(fileName)

    val adjustedXLeftTrim = (xLeftTrim * o.z()) + o.x();
    val adjustedXRightTrim = (xRightTrim * o.z()) + o.x() + o.z();
    val adjustedYTopTrim = (yTopTrim * o.w()) + o.y();
    val adjustedYBottomTrim = (yBottomTrim * o.w()) + o.y() + o.w();

    return floatArrayOf(
      adjustedXLeftTrim, adjustedYTopTrim,
      adjustedXLeftTrim, adjustedYBottomTrim,
      adjustedXRightTrim, adjustedYBottomTrim,
      adjustedXRightTrim, adjustedYTopTrim
    )
  }

  fun clear() {
    textures.clear()
//    canvas.destroy()
    canvas.resize(16, 16)
    canvas.allocate()
    availableX.clear()
    availableY.clear()
    availableX.add(padding)
    availableY.add(padding)
    size.set(16, 16)
    maxSize.set(0, 0)
  }

  fun add(name: String, fileLocation: String) {
    if (textures.containsKey(name)) throw RuntimeException("Packer: tried to add duplicate of $name")
    if (textures.containsKey(fileLocation)) throw RuntimeException("Packer: tried to add duplicate of $fileLocation")
    textures[name] = Canvas(name, fileLocation)
//    println("Packer[${this.name}]: added $fileLocation as $name")
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
//      println("packing ${textureCanvas.name}  ======================")
      while (!tetrisPack(textureCanvas)) {
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

      // textureCanvas.size.print("$name | $posX | $posY")

      for (x in 0 until sizeX) {
        for (y in 0 until sizeY) {
          val color = textureCanvas.getPixel(x, y)
          canvas.setPixel(x + posX, y + posY, color)
        }
      }
    }

    textures.values.forEach { textureCanvas ->
      textureCanvas.destroy()
    }
  }

  private fun tetrisPack(textureCanvas: Canvas): Boolean {
    var found = false

    var bestScore = Int.MAX_VALUE

    val maxX = canvas.size.x
    val maxY = canvas.size.y

    val thisWidth = textureCanvas.size.x
    val thisHeight = textureCanvas.size.y

    var bestX = padding
    var bestY = padding

    run exit@{
      availableY.forEach yLoop@{ hmm ->
        val y = hmm!!

        if (found) return@exit

        availableX.forEach xLoop@{ hmm2 ->
          val x = hmm2!!

          val newScore = x + y

//          println("n:$newScore | b:$bestScore")

          if (newScore > bestScore) return@xLoop

//          println("($x | $y)")

          if (x + thisWidth + padding >= maxX || y + thisHeight + padding >= maxY) return@xLoop

          var failed = false

          textures.values.forEach nextObject@{ other ->
            if (other.uuid == textureCanvas.uuid) return@nextObject

            val otherX = other.position.x
            val otherY = other.position.y

            val otherWidth = other.size.x
            val otherHeight = other.size.y

            if (other.packed && otherX + otherWidth + padding > x &&
              otherX < x + thisWidth + padding &&
              otherY + otherHeight + padding > y &&
              otherY < y + thisHeight + padding
            ) {

//              println("${textureCanvas.uuid} collided with ${other.uuid}")

              failed = true
              return@xLoop
            }
          }

          if (!failed) {
//            println("found")
            found = true
            bestX = x
            bestY = y
            bestScore = newScore
            return@yLoop
          }
        }
      }
    }

//    println("final score: $bestScore")

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

  fun getChannels(): Int {
    return CHANNELS
  }
}