package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.floor

object clientPlayer {
  private val position = Vector3f()
  //? note: this will create a bug that if you're sitting at the exact corner of the world, it doesn't auto scan. Who cares.
  private val oldChunkPosition = Vector2i(Int.MAX_VALUE, Int.MAX_VALUE)
  private val currentChunkPosition = Vector2i()


  fun setPosition(newPos: Vector3fc) {
    position.set(newPos)
    val x: Int = floor(newPos.x() / world.getChunkWidth()).toInt()
    val z: Int = floor(newPos.z() / world.getChunkDepth()).toInt()
    currentChunkPosition.set(x,z)
//    println("$x, $z")
    if (currentChunkPosition != oldChunkPosition) {
      world.cleanAndGenerationScan()
    }
    oldChunkPosition.set(currentChunkPosition)
  }

  fun getPosition(): Vector3fc = position

  fun getChunkPosition(): Vector2ic = currentChunkPosition




}