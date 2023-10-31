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
  private val projectedPos = Vector3f()
  private val velocity = Vector3f()
  private val oldVelocity = Vector3f()
  private val entityAABBMin = Vector3f()
  private val entityAABBMax = Vector3f()
  private val worldAABBMin = Vector3f()
  private val worldAABBMax = Vector3f()
  private val normalizedVelocity = Vector3f()

  private object directionResult {
    var left = false
    var right = false
    var front = false
    var back = false
    var down = false
    var up = false
    fun reset() {
      left = false
      right = false
      front = false
      back = false
      down = false
      up = false
    }
  }

    //? note: Entity collision.

  internal fun collideEntityToWorld(entity: GroovyEntity) {

    // Thanks luatic!
    // https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
    // https://www.amanotes.com/post/using-swept-aabb-to-detect-and-process-collision

    entity.onGround = false

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


    // Player has fallen/jumped/flown out of the map no need to detect against blocks.
    if (outOfMap(pos.y, pos.y + size.y())) {
      entity.setVelocity(velocity)
      entity.setPosition(pos.add(velocity))
      return
    }

    // If this entity exists in an area that's unloaded, freeze.
    pos.add(velocity, projectedPos)
    calculateMapRegion()
    if (!blockManipulator.set(min, max)) return


    val remainingTime = velocity.length()
    var currentTime = remainingTime % 1f
    val loops = ceil(remainingTime).toInt()
    velocity.normalize(normalizedVelocity)

//    println("loops $loops")
//    updateOldAABB()
//    println("-----")

    val blockManipulatorMin = blockManipulator.getMin()
    val blockManipulatorMax = blockManipulator.getMax()

    //note: this is a placeholder in case this ever needs to iterate in the direction the velocity.
    // if this is ever the case, use the builtin toward iterator or this won't work correctly.
    val minX = if (velocity.x < 0) blockManipulatorMin.x() else blockManipulatorMax.x()
    val minY = if (velocity.y < 0) blockManipulatorMin.y() else blockManipulatorMax.y()
    val minZ = if (velocity.z < 0) blockManipulatorMin.z() else blockManipulatorMax.z()

    val maxX = if (velocity.x < 0) blockManipulatorMax.x() else blockManipulatorMin.x()
    val maxY = if (velocity.y < 0) blockManipulatorMax.y() else blockManipulatorMin.y()
    val maxZ = if (velocity.z < 0) blockManipulatorMax.z() else blockManipulatorMin.z()

    var currentFriction = 0f

    // By block.
    (0 until loops).forEach { _ ->
      // By axis.
      (0..2).forEach { axis ->
        when (axis) {
          0 -> pos.x += (normalizedVelocity.x * currentTime)
          1 -> pos.y += (normalizedVelocity.y * currentTime)
          2 -> pos.z += (normalizedVelocity.z * currentTime)
        }
        updateEntityAABB()

        for (x in minX toward maxX) {
          for (z in minZ toward maxZ) {
            for (y in minY toward maxY) {

              val id = blockManipulator.getID(x, y, z)
//              val doubleCheck = getBlockID(x.toFloat(), y.toFloat(), z.toFloat())

//              if (doubleCheck != id) {
//                throw RuntimeException("failed. Got $id expected $doubleCheck")
//              }

              if (!block.isWalkable(id)) continue

              worldAABBMin.set(
                x.toFloat(),
                y.toFloat(),
                z.toFloat()
              )
              worldAABBMax.set(
                x + 1f,
                y + 1f,
                z + 1f
              )

              // todo: Here it would get the blockbox collision box and run through the boxes individually
              // todo: Here it would run through them individually

              directionResult.reset()

//              val oldY = pos.y
              if (!entityCollidesWithWorld(axis)) continue
              resolveCollision(axis)
              updateEntityAABB()
              if (directionResult.down) {
//                if (abs(oldY - pos.y) > 0.5f) {
//                  println("$x,$y,$z | ${floor(pos.x)},${floor(pos.y)},${floor(pos.z)} | ${pos.x}, ${pos.z}")
//                }
                currentFriction = block.getFriction(id)
                entity.onGround = true
              }
            }
          }
        }
      }
      currentTime = 1f
    }

    if (entity.onGround) {
      // Block friction.
      velocity.x = signum(velocity.x) * (abs(velocity.x) / currentFriction)
      velocity.z = signum(velocity.z) * (abs(velocity.z) / currentFriction)
      entity.friction = currentFriction
    } else {
      // Air friction.
      velocity.x = signum(velocity.x) * (abs(velocity.x) / entity.friction)
      velocity.z = signum(velocity.z) * (abs(velocity.z) / entity.friction)
    }

    entity.setVelocity(velocity)
    entity.setPosition(pos)
  }



  private fun resolveCollision(axis: Int) {
    when (axis) {
      0 -> {
        if (directionResult.left) {
//          println("left collision")
          pos.x = worldAABBMax.x + size.x + 0.0001f
          normalizedVelocity.x = 0f
          velocity.x = 0f
        } else if (directionResult.right) {
//          println("right collision")
          pos.x = worldAABBMin.x - size.x - 0.0001f
          normalizedVelocity.x = 0f
          velocity.x = 0f
        }
      }
      1 -> {
        if (directionResult.down) {
//          println("down collision")
          pos.y = worldAABBMax.y + 0.0001f
          normalizedVelocity.y = -0.001f
          velocity.y = -0.01f
        } else if (directionResult.up) {
//          println("up collision")
          pos.y = worldAABBMin.y - size.y - 0.0001f
          normalizedVelocity.y = 0f
          velocity.y = 0f
        }
      }
      2 -> {
        if (directionResult.front) {
//          println("front collision")
          pos.z = worldAABBMax.z + size.x + 0.0001f
          normalizedVelocity.z = 0f
          velocity.z = 0f
        } else if (directionResult.back) {
//          println("back collision")
          pos.z = worldAABBMin.z - size.x - 0.0001f
          normalizedVelocity.z = 0f
          velocity.z = 0f
        }
      }
    }

  }

  private fun updateEntityAABB() {
    entityAABBMin.set(
      pos.x - size.x,
      pos.y,
      pos.z - size.x
    )
    entityAABBMax.set(
      pos.x + size.x,
      pos.y + size.y,
      pos.z + size.x
    )
  }

  private fun entityCollidesWithWorld(axis: Int): Boolean {
    val noCollision =
        entityAABBMin.x > worldAABBMax.x || entityAABBMax.x < worldAABBMin.x ||
         entityAABBMin.y > worldAABBMax.y || entityAABBMax.y < worldAABBMin.y ||
         entityAABBMin.z > worldAABBMax.z || entityAABBMax.z < worldAABBMin.z

    if (noCollision) {
      return false
    }

    when (axis) {
      0 -> {
        if (velocity.x < 0f) directionResult.left = true
        if (velocity.x > 0f) directionResult.right = true
      }
      1 -> {
        if (velocity.y < 0f) directionResult.down = true
        if (velocity.y > 0f) directionResult.up = true
      }
      2 -> {
        if (velocity.z < 0f) directionResult.front = true
        if (velocity.z > 0f) directionResult.back = true
      }
    }

    return true
  }
  
  private fun outOfMap(yMin: Float, yMax: Float): Boolean = yMin >= WORLD_Y_MAX || yMax < WORLD_Y_MIN

  private fun calculateMapRegion() {
    min.x = min(projectedPos.x, oldPos.x) - size.x
    max.x = max(projectedPos.x, oldPos.x) + size.x
    min.y = min(projectedPos.y, oldPos.y)
    max.y = max(projectedPos.y, oldPos.y) + size.y
    min.z = min(projectedPos.z, oldPos.z) - size.x
    max.z = max(projectedPos.z, oldPos.z) + size.x
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