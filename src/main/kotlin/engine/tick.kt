package engine


// Thanks Rubenwardy!
object tick {
  private var accumulator = 0f
  internal const val GOAL = 30
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