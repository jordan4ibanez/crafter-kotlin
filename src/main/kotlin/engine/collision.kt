package engine

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc

object collision {
  private val chunkMatrix = Matrix4f()
  private val workerMatrix = Matrix4f()
  private val intersection = FrustumIntersection()
  private val min = Vector3f()
  private val max = Vector3f()
  private val gravity = world.getGravity()
  private var accumulator = 0f
  private var tick = false
  private const val tickGoal = 20
  private const val tickTime = 1f / tickGoal.toFloat()
  private val pos = Vector3f()
  private val oldPos = Vector3f()
  private val velocity = Vector3f()

  //? note: Entity collision.
  internal fun accumulate(delta: Float) {
    accumulator += delta
    if (accumulator >= tickTime) {
      tick = true
      accumulator -= tickTime
    }
  }

  internal fun tick(): Boolean = tick

  internal fun collideEntityToWorld(entity: GroovyEntity, delta: Float) {
    val size = entity.getSize()
    pos.set(entity.getPosition())
    velocity.set(entity.getVelocity())

    // todo: make this an accumulative function in the future.

    // I know JOML has functions to do this, I want this to be explicit for debugging.
    pos.set(
      pos.x + (velocity.x * delta),
      pos.y + (velocity.y * delta),
      pos.z + (velocity.z * delta)
    )

    //todo: if the player's top position is below 0 or the player's bottom position is equal to or greater than 128 only do movement, no collision
    // This will auto return in the future.

    oldPos.set(pos)

    // If this entity exists in an area that's unloaded, freeze.
    if (!blockManipulator.set(
      pos.x() - size.x(), pos.y(), pos.z() - size.x(),
      pos.x() + size.x(), pos.y() + size.y(), pos.z() + size.x())) return




    blockManipulator.forEach {

    }
  }






  //? note: Camera collision.

  internal fun chunkMeshWithinFrustum(x: Float, y: Float, z: Float): Boolean {
    updateChunkMatrix(x,y,z)

    //? note: Simulate the calculation that happens in GLSL on the cpu.
    return intersection
      .set(workerMatrix.set(camera.getCameraMatrix()).mul(chunkMatrix))
      .testAab(
        min.set(0f,0f,0f),
        max.set(world.getChunkWidthFloat(),world.getChunkHeightFloat(),world.getChunkDepthFloat())
      )

  }

  private fun updateChunkMatrix(x: Float, y: Float, z: Float) {
    //? note: Simulates the positions that chunks are drawn in. They are actually drawn at Y = 0.
    val camPos = camera.getPosition()
    chunkMatrix
      .identity()
      .translate(
        x - camPos.x(),
        y - camPos.y(),
        z - camPos.z()
      )
  }


  // Entity collision




}