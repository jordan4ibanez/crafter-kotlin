package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.floor

object clientPlayer : Mob(Vector3f(0f,0f,0f)) {
  //? note: this will create a bug that if you're sitting at the exact corner of the world, it doesn't auto scan. Who cares.
  private val oldChunkPosition = Vector2i(Int.MAX_VALUE, Int.MAX_VALUE)
  private val currentChunkPosition = Vector2i()

  //! Fixme: TURN THIS OFF!
//  private var payloaded = false

  override fun setPosition(newPosition: Vector3fc) {
    position.set(newPosition)
    val x: Int = floor(newPosition.x() / world.getChunkWidth()).toInt()
    val z: Int = floor(newPosition.z() / world.getChunkDepth()).toInt()
    currentChunkPosition.set(x,z)
//    println("$x, $z")
    //! FIXME: TURN THIS OFF!
    if (currentChunkPosition != oldChunkPosition /*&& !payloaded*/) {
      world.cleanAndGenerationScan()
//      payloaded = true
    }
    oldChunkPosition.set(currentChunkPosition)
  }

  fun getChunkPosition(): Vector2ic = currentChunkPosition




}