package engine.entity.groovy_entity

import engine.collision_box.collisionBox
import engine.entity.*
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.*

open class GroovyEntity : Entity {

  // Thanks, GreenXenith!
  open val classifier = "undefined"
  val uuid = UUID.randomUUID().toString()
  private val size = Vector2f(1f, 1f)
  private val rotation = Vector3f()

  fun drawCollisionBox() {
    collisionBox.draw(interpolationPosition, size)
//    collisionBox.draw(position, size)
  }

  fun getSize(): Vector2fc = size
  open fun setSize(newSize: Vector2fc) {
    size.set(newSize)
  }

  fun getRotation(): Vector3fc = rotation
  open fun setRotation(newRotation: Vector3fc) {
    rotation.set(newRotation)
  }


  fun mobMove(goalX: Float, goalZ: Float, speedGoal: Float, snappiness: Float = 1f) =
    mobMove(vector2Worker.set(goalX, goalZ), speedGoal, snappiness)

  fun mobMove(goalDir: Vector2fc, speedGoal: Float, snappiness: Float = 1f) {

    goalVel.set(goalDir).normalize().mul(speedGoal)

    val currentVel = getVelocity()
    val currentAcceleration = getAcceleration()

    vel2d.set(currentVel.x(), currentVel.z())

    goalVel.sub(vel2d, diff)
    diff.mul(friction * snappiness)

    accelerationWorker.set(
      diff.x,
      currentAcceleration.y(),
      diff.y
    )
    if (!accelerationWorker.isFinite) accelerationWorker.set(0f, currentAcceleration.y(), 0f)
    setAcceleration(accelerationWorker)
  }

  constructor(pos: Vector3fc) : super(pos)

  open fun onSpawn() {}
  open fun onDespawn() {}
  open fun onHit() {}
  open fun onRightClick() {}
}