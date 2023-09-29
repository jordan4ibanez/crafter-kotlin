package engine

object keyboard {
  private var lastKey: Char = '\u0000'

  val currentMap = HashMap<Int, Boolean>()
  val memoryMap = HashMap<Int, Boolean>()

  val memoryFlush = ArrayDeque<Int>()


}