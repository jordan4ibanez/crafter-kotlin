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
  private val oldAABBMin = Vector3f()
  private val oldAABBMax = Vector3f()
  private val entityAABBMin = Vector3f()
  private val entityAABBMax = Vector3f()
  private val worldAABBMin = Vector3f()
  private val worldAABBMax = Vector3f()
  private val normal = Vector3f()
  private var foundDir = Direction.NONE
  private val normalizedVelocity = Vector3f()
//  private val newPosition = Vector3f()

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
    updateOldAABB()

    (0 until loops).forEach { _ ->
      pos.set(
        pos.x + (normalizedVelocity.x * currentTime),
        pos.y + (normalizedVelocity.y * currentTime),
        pos.z + (normalizedVelocity.z * currentTime)
      )
      updateEntityAABB()
      var index = 0
      blockManipulator.forEach blockLoop@{
        val id = it.getBlockID()
        if (block.isWalkable(id)) {
          val rootPos = blockManipulator.indexToPos(index)
          worldAABBMin.set(rootPos.x.toFloat(), rootPos.y.toFloat(), rootPos.z.toFloat())
          worldAABBMax.set(rootPos.x.toFloat() + 1f, rootPos.y.toFloat() + 1f, rootPos.z.toFloat() + 1f)
          // todo: Here it would get the blockbox collision box and run through the boxes individually
          // todo: Here it would run through them individually
          if (!entityCollidesWithWorld()) return@blockLoop
          oldPos.set(pos)
          resolveCollision()
          updateEntityAABB()
          updateOldAABB()
//          println("pos is now: ${pos.y}")

        }
        index++
      }
      currentTime += 1f
//      println("current time: $currentTime")
    }

    entity.setVelocity(velocity)
    entity.setPosition(pos)
//    println("pos is being set at ${pos.y}")
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
  private fun updateOldAABB() {
    oldAABBMin.set(
      oldPos.x - size.x,
      oldPos.y,
      oldPos.z - size.x
    )
    oldAABBMax.set(
      oldPos.x + size.x,
      oldPos.y + size.y,
      oldPos.z + size.x
    )
  }

  private fun resolveCollision() {
    println(foundDir)
    if (foundDir == Direction.DOWN || foundDir == Direction.UP) {
      if (velocity.y <= 0) {
        pos.y = worldAABBMax.y + 0.001f
        normalizedVelocity.y = 0f
        velocity.y = -0.01f
      } else {
        pos.y = worldAABBMin.y - size.y - 0.001f
        normalizedVelocity.y = 0f
        velocity.y = 0.01f
      }
    }
  }

  private fun entityCollidesWithWorld(): Boolean {
    val collision = !(entityAABBMin.x > worldAABBMax.x || entityAABBMax.x < worldAABBMin.x || entityAABBMin.y > worldAABBMax.y || entityAABBMax.y < worldAABBMin.y || entityAABBMin.z > worldAABBMax.z || entityAABBMax.z < worldAABBMin.z)
    if (!collision) {
      foundDir = Direction.NONE
      return false
    }

//    println("test: ${oldAABBMin.y} | ${entityAABBMin.y} | ${worldAABBMax.y}")

    val leftWasOut = oldAABBMin.x > worldAABBMax.x
    val rightWasOut = oldAABBMax.x < worldAABBMin.x
    val bottomWasOut = oldAABBMin.y > worldAABBMax.y
    val topWasOut = oldAABBMax.y < worldAABBMin.y
    val frontWasOut = oldAABBMin.z > worldAABBMax.z
    val backWasOut = oldAABBMax.z < worldAABBMin.z

    val leftIsIn = entityAABBMin.x < worldAABBMax.x
    val rightIsIn = entityAABBMax.x > worldAABBMin.x
    val bottomIsIn = entityAABBMin.y < worldAABBMax.y
    val topIsIn = entityAABBMax.y > worldAABBMin.y
    val frontIsIn = entityAABBMin.z < worldAABBMax.z
    val backIsIn = entityAABBMax.z > worldAABBMin.z

    foundDir = when {
      leftWasOut && leftIsIn -> Direction.LEFT
      rightWasOut && rightIsIn -> Direction.RIGHT
      bottomWasOut && bottomIsIn -> Direction.DOWN
      topWasOut && topIsIn -> Direction.UP
      frontWasOut && frontIsIn -> Direction.FRONT
      backWasOut && backIsIn -> Direction.BACK
      else -> Direction.NONE
    }
    return true
  }

//  private fun calculateNormal(position: Vector3ic) {
//    if (velocity.x() <= 0f) {
//      normal.x = position.x() + 1f /*fixme: use size here*/
//    } else {
//      normal.x = position.x().toFloat()
//    }
//    if (velocity.y() <= 0f) {
//      normal.y = position.y() + 1f /*fixme: use size here*/
//    } else {
//      normal.y = position.y().toFloat()
//    }
//    if (velocity.z() <= 0f) {
//      normal.z = position.z() + 1f /*fixme: use size here*/
//    } else {
//      normal.z = position.z().toFloat()
//    }
//  }

  private fun outOfMap(yMin: Float, yMax: Float): Boolean = yMin >= WORLD_Y_MAX || yMax < WORLD_Y_MIN

  private fun calculateMapRegion() {
    //fixme: this is unoptimized.
    if (velocity.x() <= 0) {
      min.x = projectedPos.x - size.x
      max.x = oldPos.x + size.x
    } else {
      min.x = oldPos.x - size.x
      max.x = projectedPos.x + size.x
    }
    if (velocity.y() <= 0) {
      min.y = projectedPos.y
      max.y = oldPos.y + size.y
    } else {
      min.y = oldPos.y
      max.y = projectedPos.y + size.y
    }
    if (velocity.z() <= 0) {
      min.z = projectedPos.z - size.x
      max.z = oldPos.z + size.x
    } else {
      min.z = oldPos.z - size.x
      max.z = projectedPos.z + size.x
    }

//    println("map region Y: min: ${min.y} | max: ${max.y}")
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