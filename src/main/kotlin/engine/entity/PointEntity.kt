package engine.entity

import engine.world.world
import org.joml.Vector3f
import org.joml.Vector3fc


open class PointEntity {

  internal var interpolationTimer = 0f
  internal val oldPosition = Vector3f()
  internal val interpolationPosition = Vector3f()
  val position = Vector3f()
  private val velocity = Vector3f()
  private val acceleration = Vector3f(0f, -world.getGravity(), 0f)
  var meshID = 0
  var onGround = false
  internal var friction = 0.8f

  constructor(pos: Vector3fc) {
    this.position.set(pos)
    oldPosition.set(pos)
    interpolationPosition.set(pos)
  }

  fun getFriction(): Float = friction

  fun getPosition(): Vector3fc = position
  open fun setPosition(newPosition: Vector3fc) {
    oldPosition.set(position)
    position.set(newPosition)
    interpolationTimer = 0f
  }

  internal fun interpolate(delta: Float) {
    if (interpolationTimer >= 1f) {
//      println("interpolation is complete $interpolationTimer")
      return
    }
//    println("interpolating thing $interpolationTimer")
    interpolationTimer += delta * interpolationSnappiness
//    println("interpolation reached $interpolationTimer")
    if (interpolationTimer >= 1f) {
//      println("interpolation completed")
      interpolationTimer = 1f
    }
    oldPosition.lerp(position, interpolationTimer, interpolationPosition)
  }

  fun getVelocity(): Vector3fc = velocity
  fun setVelocity(x: Float, y: Float, z: Float) = setVelocity(vector3Worker.set(x, y, z))
  open fun setVelocity(newVelocity: Vector3fc) {
    velocity.set(newVelocity)
  }

  fun addVelocity(moreVelocity: Vector3fc) = addVelocity(moreVelocity.x(), moreVelocity.y(), moreVelocity.z())
  fun addVelocity(x: Float, y: Float, z: Float) = velocity.add(x, y, z)

  fun getAcceleration(): Vector3fc = acceleration
  fun setAcceleration(x: Float, y: Float, z: Float) = setAcceleration(vector3Worker.set(x, y, z))
  open fun setAcceleration(newAcceleration: Vector3fc) {
    acceleration.set(newAcceleration)
  }

  open fun onTick(delta: Float) {}
}