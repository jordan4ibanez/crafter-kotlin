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
//  private var foundDir = Direction.NONE
  private val normalizedVelocity = Vector3f()
//  private val newPosition = Vector3f()

//  enum class Direction {
//    NONE,LEFT, RIGHT, FRONT, BACK, DOWN, UP
//  }
  object directionResult {
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

//    println("X = ${velocity.x} | Z = ${velocity.z}")

    oldPos.set(pos)
    oldVelocity.set(velocity)

    // Gravity.
    velocity.y -= gravity

    // Friction. todo: add in block friction
    velocity.x = signum(velocity.x) * (abs(velocity.x) / 1.2f)
    velocity.z = signum(velocity.z) * (abs(velocity.z) / 1.2f)

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
//    println("-----")

    val blockManipulatorMin = blockManipulator.getMin()
    val blockManipulatorMax = blockManipulator.getMax()

    val minX = blockManipulatorMin.x()
    val minY = blockManipulatorMin.y()
    val minZ = blockManipulatorMin.z()
    val maxX = blockManipulatorMax.x()
    val maxY = blockManipulatorMax.y()
    val maxZ = blockManipulatorMax.z()

    (0 until loops).forEach { _ ->
      pos.set(
        pos.x + (normalizedVelocity.x * currentTime),
        pos.y + (normalizedVelocity.y * currentTime),
        pos.z + (normalizedVelocity.z * currentTime)
      )
      updateEntityAABB()

      //? note: Might have to iterate through this in the direction of the velocity.

      for (x in minX..maxX) {
        for (y in minY..maxY) {
          for (z in minZ..maxZ) {
            val id = blockManipulator.getID(x,y,z)
            if (block.isWalkable(id)) {
              worldAABBMin.set(x.toFloat(), y.toFloat(), z.toFloat())
              worldAABBMax.set(x.toFloat() + 1f, y.toFloat() + 1f, z.toFloat() + 1f)
              // todo: Here it would get the blockbox collision box and run through the boxes individually
              // todo: Here it would run through them individually

              directionResult.reset()

              if (!entityCollidesWithWorld()) continue
              oldPos.set(pos)
              resolveCollision()
              updateEntityAABB()
              entity.onGround = directionResult.down
            }
          }
        }
      }
      currentTime += 1f
    }
    entity.setVelocity(velocity)
    entity.setPosition(pos)
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
    if (directionResult.down) {
      pos.y = worldAABBMax.y + 0.001f
      normalizedVelocity.y = 0f
      velocity.y = -0.01f
    }
    if (directionResult.up) {
      pos.y = worldAABBMin.y - size.y - 0.001f
      normalizedVelocity.y = 0f
      velocity.y = 0.01f
    }
  }

  private fun entityCollidesWithWorld(): Boolean {
    val collision = !(entityAABBMin.x > worldAABBMax.x ||
        entityAABBMax.x < worldAABBMin.x ||
        entityAABBMin.y > worldAABBMax.y ||
        entityAABBMax.y < worldAABBMin.y ||
        entityAABBMin.z > worldAABBMax.z ||
        entityAABBMax.z < worldAABBMin.z)

    if (!collision) return false


    val leftWasOut   = oldAABBMin.x > worldAABBMax.x
    val rightWasOut  = oldAABBMax.x < worldAABBMin.x
    val bottomWasOut = oldAABBMin.y > worldAABBMax.y
    val topWasOut    = oldAABBMax.y < worldAABBMin.y
    val frontWasOut  = oldAABBMin.z > worldAABBMax.z
    val backWasOut   = oldAABBMax.z < worldAABBMin.z

    val leftIsIn   = entityAABBMin.x < worldAABBMax.x
    val rightIsIn  = entityAABBMax.x > worldAABBMin.x
    val bottomIsIn = entityAABBMin.y < worldAABBMax.y
    val topIsIn    = entityAABBMax.y > worldAABBMin.y
    val frontIsIn  = entityAABBMin.z < worldAABBMax.z
    val backIsIn   = entityAABBMax.z > worldAABBMin.z


    if (leftWasOut && leftIsIn) directionResult.left = true
    if (rightWasOut && rightIsIn) directionResult.right = true
    if (bottomWasOut && bottomIsIn) directionResult.down = true
    if (topWasOut && topIsIn) directionResult.up = true
    if (frontWasOut && frontIsIn) directionResult.front = true
    if (backWasOut && backIsIn) directionResult.back = true

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