package engine.delta_time

object DeltaTime {

  // Time keeper object.

  private var deltaTime: Float = 0f
  private var oldTime: Long = System.nanoTime()

  private var fps = 0
  private var fpsAccumulator = 0
  private var fpsTime = 0f
  private var fpsUpdate = false

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
    fpsAccumulator++
  }

  // Exposed API to Groovy.
  fun getDelta(): Float {
    return deltaTime
  }

  fun fpsUpdated(): Boolean {
    return fpsUpdate
  }

  fun getFPS(): Int {
    return fps
  }
}