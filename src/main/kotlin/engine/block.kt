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
private val drawtype: ConcurrentHashMap<String, String> = TODO()
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