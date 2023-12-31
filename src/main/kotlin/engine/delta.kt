package engine

internal object delta {

  // Time keeper object.

  internal var deltaTime: Float = 0f
  private var oldTime: Long = System.nanoTime()

  internal var fps = 0
  private var fpsAccumulator = 0
  private var fpsTime = 0f
  internal var fpsUpdate = false

  internal fun calculate() {
    // No other packages should be able to touch this besides engine.
    val currentTime = System.nanoTime()
    deltaTime = (currentTime - oldTime).toFloat() / 1_000_000_000f
    oldTime = currentTime

    fpsCalculate()
  }

  private fun fpsCalculate() {
    fpsUpdate = false
    fpsTime += deltaTime
    if (fpsTime >= 1f) {
      fpsTime -= 1f
      fps = fpsAccumulator
      fpsUpdate = true
      fpsAccumulator = 0
    }
    fpsAccumulator++;
  }
}

// Hook this up as a general function. An alias so you do not have to do: delta.getDelta()
fun getDelta(): Float {
  return delta.deltaTime
}

fun fpsUpdated(): Boolean {
  return delta.fpsUpdate
}

fun getFPS(): Int {
  return delta.fps
}