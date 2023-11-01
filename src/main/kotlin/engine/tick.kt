package engine


// Thanks Rubenwardy!
object tick {
  private var accumulator = 0f
  internal const val GOAL = 30
  private  const val goalDelta = 1f / GOAL.toFloat()
  internal fun think(delta: Float): Boolean {
    accumulator += delta

    if (accumulator >= goalDelta) {
      accumulator -= goalDelta
      if (accumulator > goalDelta) accumulator = goalDelta % accumulator
      return true
    }
    return false
  }
}