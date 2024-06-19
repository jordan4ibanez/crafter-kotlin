package engine.keyboard

object keyboard {

  internal var lastKey: Char = '\u0000'

  private val currentMap = HashMap<Int, Boolean>()
  private val memoryMap = HashMap<Int, Boolean>()

  val memoryFlush = ArrayDeque<Int>()

  internal fun pollMemory() {
    while (!memoryFlush.isEmpty()) {
      setMemory(memoryFlush.removeFirst())
    }
  }

  fun hasTyped(): Boolean {
    return lastKey != '\u0000'
  }

  fun getLastInput(): Char {
    if (!hasTyped()) {
      throw RuntimeException("keyboard: Tried to poll nothing. Check if hasTyped.")
    }
    val gotten = lastKey
    lastKey = '\u0000'
    return gotten
  }

  fun isDown(key: Int): Boolean {
    return getCurrent(key)
  }

  fun isPressed(key: Int): Boolean {
    return getCurrent(key) && !getMemory(key)
  }

  internal fun setCurrent(key: Int, action: Boolean) {
    currentMap[key] = action
  }

  internal fun setMemory(key: Int) {
    if (!currentMap.containsKey(key)) {
      memoryMap[key] = false
      return
    }

    memoryMap[key] = currentMap[key]!!
  }

  private fun getCurrent(key: Int): Boolean {
    return currentMap[key] ?: run {
      currentMap[key] = false
      false
    }
  }

  private fun getMemory(key: Int): Boolean {
    return memoryMap[key] ?: run {
      memoryMap[key] = false
      return false
    }
  }
}