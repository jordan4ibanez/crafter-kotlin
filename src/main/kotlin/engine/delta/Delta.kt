package engine.delta

object Delta {

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

  // Exposed API to Groovy.
  fun getDelta(): Float {
    return Delta.deltaTime
  }

  fun fpsUpdated(): Boolean {
    return Delta.fpsUpdate
  }

  fun getFPS(): Int {
    return Delta.fps
  }
}