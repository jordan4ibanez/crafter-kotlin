package engine

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
  private val size = Vector2f()
  private val pos = Vector3f()
  private val oldPos = Vector3f()
  private val projectedPos = Vector3f()
  private val velocity = Vector3f()
  private val acceleration = Vector3f()
  private val oldVelocity = Vector3f()
  private val entityAABBMin = Vector3f()
  private val entityAABBMax = Vector3f()
  private val worldAABBMin = Vector3f()
  private val worldAABBMax = Vector3f()
  private val normalizedVelocity = Vector3f()
  private const val TICK_DELTA = tick.GOAL_DELTA

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
    acceleration.set(entity.getAcceleration()).mul(TICK_DELTA)


    oldPos.set(pos)
    oldVelocity.set(velocity)

    // Apply acceleration.
    velocity.add(acceleration)



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

    // This is debugging code.
//    val minX = blockManipulatorMin.x()
//    val minY = blockManipulatorMin.y()
//    val minZ = blockManipulatorMin.z()
//    val maxX = blockManipulatorMax.x()
//    val maxY = blockManipulatorMax.y()
//    val maxZ = blockManipulatorMax.z()
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

//        var indexCheck = 0

        for (x in minX toward maxX) {
          for (z in minZ toward maxZ) {
            for (y in minY toward maxY) {

              val id = blockManipulator.getID(x, y, z)
//              val doubleCheck = getBlockID(x.toFloat(), y.toFloat(), z.toFloat())

//              val index = blockManipulator.posToIndex(x,y,z)

//              if (index != indexCheck) {
//                val bmSize = blockManipulator.getSize()
//                throw RuntimeException("index error: In: $indexCheck | out: $index\n" +
//                  "bmsize: ${bmSize.x()}, ${bmSize.y()}, ${bmSize.z()}")
//              } else {
//                val bmSize = blockManipulator.getSize()
//                println("works with: bmsize: ${bmSize.x()}, ${bmSize.y()}, ${bmSize.z()}")
//              }
//
//              val tripleCheck = blockManipulator.indexToPos(index)

//              if (tripleCheck.x() != x || tripleCheck.y() != y || tripleCheck.z() != z) {
//                println("minmax: $minX .. $maxX")
//                println("minmax: $minY .. $maxY")
//                println("minmax: $minZ .. $maxZ")
//                throw RuntimeException("in: $x, $y, $z | out: ${tripleCheck.x()}, ${tripleCheck.y()}, ${tripleCheck.z()}")
//              }
//              indexCheck++
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

//    if (entity.onGround) {
//      // Block friction.
//      val finalVelocity = Vector2f(velocity.x, velocity.z)
//      val currentSpeed = finalVelocity.length()
//      if (!currentSpeed.isNaN()) {
//        val newSpeed = clamp(0f, MAX_SPEED, currentSpeed - currentFriction)
//        finalVelocity.normalize().mul(newSpeed)
//        if (finalVelocity.x.isNaN()) finalVelocity.x = 0f
//        if (finalVelocity.y.isNaN()) finalVelocity.y = 0f
//        velocity.x = finalVelocity.x
//        velocity.z = finalVelocity.y
//      }
//      entity.friction = currentFriction
//    } else {
//      // Air friction.
//      val finalVelocity = Vector2f(velocity.x, velocity.z)
//      val currentSpeed = finalVelocity.length()
//      if (!currentSpeed.isNaN()) {
//        val newSpeed = clamp(0f, MAX_SPEED, currentSpeed - entity.friction)
//        finalVelocity.normalize().mul(newSpeed)
//        if (finalVelocity.x.isNaN()) finalVelocity.x = 0f
//        if (finalVelocity.y.isNaN()) finalVelocity.y = 0f
//        velocity.x = finalVelocity.x
//        velocity.z = finalVelocity.y
//      }
//    }


    if (currentFriction > 0f) {
      entity.friction = currentFriction
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