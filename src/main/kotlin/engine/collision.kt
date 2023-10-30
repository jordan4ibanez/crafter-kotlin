package engine

import engine.world.getBlockID
import org.joml.*
import org.joml.Math.*

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
  private val size = Vector2f()
  private val pos = Vector3f()
  private val oldPos = Vector3f()
  private val velocity = Vector3f()
  private val oldVelocity = Vector3f()
//  private val entityAABBMin = Vector3f()
//  private val entityAABBMax = Vector3f()
//  private val worldAABBMin = Vector3f()
//  private val worldAABBMax = Vector3f()
  private val normal = Vector3f()
  private var foundDir = Direction.NONE
  enum class Direction {
    NONE,LEFT, RIGHT, FRONT, BACK, DOWN, UP
  }

    //? note: Entity collision.

  internal fun collideEntityToWorld(entity: GroovyEntity) {

    // Thanks luatic!
    // https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
    // https://www.amanotes.com/post/using-swept-aabb-to-detect-and-process-collision

    size.set(entity.getSize())
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


    var index = 0
    blockManipulator.forEach {
      val id = it.getBlockID()
      if (block.isWalkable(id)) {
        calculateNormal(blockManipulator.indexToPos(index))
        val collisionTime = sweptAABB()
        pos.x = oldPos.x + (velocity.x * collisionTime)
        pos.y = oldPos.y + (velocity.y * collisionTime)
        velocity.x = 0f
        velocity.y = 0f
        println("collision occurred in dir $foundDir")
      }
      index++
    }

    entity.setVelocity(velocity)
    entity.setPosition(pos)
  }

  private fun sweptAABB(): Float {

    // fixme: find z

    // fixme: break this up into multiple functions

    // fixme: Turn this into a bunch of "when" statements because this looks horrible

    // Find the distance between the objects on the near and far sides for both x and y.

    val xInvEntry: Float
    val yInvEntry: Float
    val xInvExit: Float
    val yInvExit: Float

    when {
      (velocity.x > 0f) -> {
        xInvEntry = pos.x - (oldPos.x + size.x)
        xInvExit = (pos.x + size.x) - oldPos.x
      }

      else -> {
        xInvEntry = (pos.x + size.x) - oldPos.x
        xInvExit = pos.x - (oldPos.x + size.x)
      }
    }

    when {
      (velocity.y > 0f) -> {
        yInvEntry = pos.y - (oldPos.y + size.y)
        yInvExit = (pos.y + size.y) - oldPos.y
      }

      else -> {
        yInvEntry = (pos.y + size.y) - oldPos.y
        yInvExit = pos.y - (oldPos.y + size.y)
      }
    }

//    if (yInvEntry != 0f || xInvEntry != 0f) {
//      println("------------------------------")
//      println("entry: $xInvEntry | $yInvEntry")
//      println("exit:  $xInvExit  | $yInvExit")
//    }

    // Find time of collision and time of leaving for each axis (if statement is to prevent divide by zero) NaN on JVM.

    val xEntry: Float
    val yEntry: Float
    val xExit: Float
    val yExit: Float

    when {
      (velocity.x == 0f) -> {
        xEntry = Float.NEGATIVE_INFINITY
        xExit = Float.POSITIVE_INFINITY
      }
      else -> {
        xEntry = xInvEntry / velocity.x
        xExit = xInvExit / velocity.x
      }
    }

    if (velocity.y == 0f) {
      yEntry = Float.NEGATIVE_INFINITY
      yExit = Float.POSITIVE_INFINITY
    } else {
      yEntry = yInvEntry / velocity.y
      yExit = yInvExit / velocity.y
    }

    // Find the earliest/latest times of collision float.
    val entryTime = max(xEntry, yEntry)
    val exitTime = min(xExit, yExit)

    // If there was no collision.
    when {
      (entryTime > exitTime || xEntry < 0.0f && yEntry < 0.0f || xEntry > 1.0f || yEntry > 1.0f) -> {
        normal.x = 0.0f
        normal.y = 0.0f
        foundDir = Direction.NONE
        return 1.0f
      }

      else -> {
        // If there was a collision.
        // Calculate normal of collided surface.
        when {
          (xEntry > yEntry) -> {
            when {
              (xInvEntry < 0.0f) -> {
                normal.x = 1.0f
                normal.y = 0.0f
              }

              else -> {
                normal.x = -1.0f
                normal.y = 0.0f
              }
            }
          }

          else -> {
            when {
              (yInvEntry < 0.0f) -> {
                normal.x = 0.0f
                normal.y = 1.0f
              }

              else -> {
                normal.x = 0.0f
                normal.y = -1.0f
              }
            }
          }
        }
      }
    }

    // FIXME: needs front and back
    if (xEntry > yEntry) {
      if (xInvEntry > 0f) {
        foundDir = Direction.RIGHT
      } else {
        foundDir = Direction.LEFT
      }
    } else {
      if (xInvEntry > 0f) {
        foundDir = Direction.UP
      } else {
        foundDir = Direction.DOWN
      }
    }

    // Return the time of collision return entryTime.
    return entryTime
  }

  private fun calculateNormal(position: Vector3ic) {
    if (velocity.x() <= 0) {
      normal.x = position.x() + 1f /*fixme: use size here*/
    } else {
      normal.x = position.x().toFloat()
    }
    if (velocity.y() <= 0) {
      normal.y = position.y() + 1f /*fixme: use size here*/
    } else {
      normal.y = position.y().toFloat()
    }
    if (velocity.z() <= 0) {
      normal.z = position.z() + 1f /*fixme: use size here*/
    } else {
      normal.z = position.z().toFloat()
    }
  }

  private fun outOfMap(yMin: Float, yMax: Float): Boolean = yMin >= WORLD_Y_MAX || yMax < WORLD_Y_MIN

  private fun calculateMapRegion(size: Vector2fc) {
    //fixme: this is unoptimized.
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