package engine

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Vector3f

object collision {
  private val chunkMatrix = Matrix4f()
  private val workerMatrix = Matrix4f()
  private val intersection = FrustumIntersection()
  private val min = Vector3f()
  private val max = Vector3f()
  private val gravity = world.getGravity()
  private val pos = Vector3f()
  private val oldPos = Vector3f()
  private val velocity = Vector3f()
  private const val MAX_SPEED = 1f
  private const val WORLD_Y_MAX = world.HEIGHT
  private const val WORLD_Y_MIN = 0

  //? note: Entity collision.

  internal fun collideEntityToWorld(entity: GroovyEntity) {

    val size = entity.getSize()
    pos.set(entity.getPosition())
    velocity.set(entity.getVelocity())

    oldPos.set(pos)
    pos.add(velocity)
    if (velocity.length() > MAX_SPEED) velocity.normalize().mul(MAX_SPEED)

    // Player has fallen/jumped/flown out of the map no need to detect against blocks.
    if (outOfMap(pos.y, pos.y + size.y())) return

    //todo: if the player's top position is below 0 or the player's bottom position is equal to or greater than 128 only do movement, no collision
    // This will auto return in the future.



    // gravity


    // If this entity exists in an area that's unloaded, freeze.
    if (!blockManipulator.set(
      pos.x() - size.x(), pos.y(), pos.z() - size.x(),
      pos.x() + size.x(), pos.y() + size.y(), pos.z() + size.x())) return




    blockManipulator.forEach {

    }
  }

  fun outOfMap(yMin: Float, yMax: Float): Boolean = yMin >= WORLD_Y_MAX || yMax < WORLD_Y_MIN






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