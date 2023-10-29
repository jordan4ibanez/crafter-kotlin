package engine

import engine.world.getBlockID
import org.joml.*
import org.joml.Math.abs
import org.joml.Math.ceil

object collision {
  private const val MAX_SPEED = 10f
  private const val WORLD_Y_MAX = world.HEIGHT
  private const val WORLD_Y_MIN = 0
  private val chunkMatrix = Matrix4f()
  private val workerMatrix = Matrix4f()
  private val intersection = FrustumIntersection()
  private val min = Vector3f()
  private val max = Vector3f()
  private val gravity = world.getGravity() / 100f
  private val pos = Vector3f()
  private val oldPos = Vector3f()
  private val velocity = Vector3f()
  private val oldVelocity = Vector3f()

  //? note: Entity collision.

  internal fun collideEntityToWorld(entity: GroovyEntity) {

    // Thanks luatic!
    // https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/

    val size = entity.getSize()
    pos.set(entity.getPosition())
    velocity.set(entity.getVelocity())

    oldPos.set(pos)
    oldVelocity.set(velocity)

    // Gravity.
    velocity.y -= gravity

    // Limit the speed to X blocks per tick.
    if (velocity.length() > MAX_SPEED) {
      velocity.normalize().mul(MAX_SPEED)
    }

    pos.add(velocity)

    // Player has fallen/jumped/flown out of the map no need to detect against blocks.
    if (outOfMap(pos.y, pos.y + size.y())) {

      entity.setVelocity(velocity)
      entity.setPosition(pos)
      return
    }

    //todo: if the player's top position is below 0 or the player's bottom position is equal to or greater than 128 only do movement, no collision
    // This will auto return in the future.

    // If this entity exists in an area that's unloaded, freeze.
    calculateMapRegion(size)
    if (!blockManipulator.set(min, max)) return


    blockManipulator.forEach {
      val id = it.getBlockID()
      if (block.isWalkable(id)) {
        println(block.getName(id))
        velocity.y = 0.5f
      }
    }

    entity.setVelocity(velocity)
    entity.setPosition(pos)
  }

  private fun outOfMap(yMin: Float, yMax: Float): Boolean = yMin >= WORLD_Y_MAX || yMax < WORLD_Y_MIN

  private fun calculateMapRegion(size: Vector2fc) {
    if (velocity.x() <= 0) {
      min.x = pos.x - size.x()
      max.x = oldPos.x + size.x()
    } else {
      min.x = oldPos.x - size.x()
      max.x = pos.x + size.x()
    }
    if (velocity.y() <= 0) {
      min.y = pos.y
      max.y = oldPos.y + size.y()
    } else {
      min.y = oldPos.y
      max.y = pos.y + size.y()
    }
    if (velocity.z() <= 0) {
      min.z = pos.z - size.x()
      max.z = oldPos.z + size.x()
    } else {
      min.z = oldPos.z - size.x()
      max.z = pos.z + size.x()
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