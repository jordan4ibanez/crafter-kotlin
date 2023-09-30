package engine

import java.util.concurrent.ConcurrentHashMap

/*
A concurrent component system to handle blocks.
Basic functional interface.
*/

//note: con stands for container.
private fun<T> con(): ConcurrentHashMap<String, T> {
  return ConcurrentHashMap<String, T>()
}

// Required components.
private val id       = con<Int>()
private val name     = con<String>()
private val textures = con<Array<String>>()

// Optional components.
private val drawType           = con<DrawType>()
private val walkable           = con<Boolean>()
private val liquid             = con<Boolean>()
private val flow               = con<Int>()
private val viscosity          = con<Int>()
private val climbable          = con<Boolean>()
private val sneakJumpClimbable = con<Boolean>()
private val falling            = con<Boolean>()
private val clear              = con<Boolean>()
private val damagePerSecond    = con<Int>()
private val light              = con<Int>()

enum class DrawType(val data: Int) {
  AIR(0),
  BLOCK(1),
  BLOCK_BOX(2),
  TORCH(3),
  LIQUID_SOURCE(4),
  LIQUID_FLOW(5),
  GLASS(6),
  PLANT(7),
  LEAVES(8);

  fun value(): Int {
    return data
  }
}

fun Int.toDrawType(): DrawType {
  return DrawType.entries.filter { it.data == this }.ifEmpty { throw RuntimeException("$this is not in range of drawtypes (0..8)") }[0]
}