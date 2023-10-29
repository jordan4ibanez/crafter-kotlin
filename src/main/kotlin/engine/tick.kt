package engine

object tick {
  private var accumulator = 0f
  private const val GOAL = 20
  private  const val DELTA = 1f / GOAL.toFloat()
  internal fun think(delta: Float): Boolean {
    accumulator += delta
    if (accumulator >= DELTA) {
      accumulator -= DELTA
      return true
    }
    return false
  }
}