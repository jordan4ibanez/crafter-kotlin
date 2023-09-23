package engine

internal object delta {

  // Time keeper object.

  internal var deltaTime: Float = 0f
  private var oldTime: Long = System.nanoTime()

  internal fun calculate() {
    // No other packages should be able to touch this besides engine.
    val currentTime = System.nanoTime()
    deltaTime = (currentTime - oldTime).toFloat() / 1_000_000_000f
    oldTime = currentTime
  }
}

// Hook this up as a general function. An alias so you do not have to do: delta.getDelta()
fun getDelta(): Float {
  return delta.deltaTime
}