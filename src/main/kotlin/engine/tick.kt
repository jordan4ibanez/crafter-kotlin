package engine


// Thanks Rubenwardy!
object tick {
  private var accumulator = 0f
  internal const val GOAL = 30
  internal const val GOAL_DELTA = 1f / GOAL.toFloat()
  internal fun think(delta: Float): Boolean {
    accumulator += delta

    if (accumulator >= GOAL_DELTA) {
      accumulator -= GOAL_DELTA
      if (accumulator > GOAL_DELTA) accumulator = GOAL_DELTA % accumulator
      return true
    }
    return false
  }
}