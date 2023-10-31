package engine

import org.joml.Math.*
import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector3i
import org.lwjgl.glfw.GLFW
import kotlin.math.floor

object clientPlayer : Player(Vector3f(0f,110f,0f), "singleplayer") {

  //? note: this will create a bug that if you're sitting at the exact corner of the world, it doesn't auto scan. Who cares.
  private val oldChunkPosition = Vector2i(Int.MAX_VALUE, Int.MAX_VALUE)
  private val currentChunkPosition = Vector2i()
  private val positionBuffer = Vector3i()

  fun initialize() {
    // Automatically add in the client player into players.
    entity.addPlayer(this)
  }

  // Future note: This can be used to turn off the automatic chunk generation (for prototyping and generation debugging).
//  private var payloaded = false

  override fun setPosition(newPosition: Vector3fc) {

    oldPosition.set(position)
    position.set(newPosition)
    interpolationTimer = 0f
    val x: Int = floor(newPosition.x() / world.getChunkWidth()).toInt()
    val z: Int = floor(newPosition.z() / world.getChunkDepth()).toInt()
    currentChunkPosition.set(x,z)
//    println("$x, $z")

    if (currentChunkPosition != oldChunkPosition /*&& !payloaded*/) {
      world.cleanAndGenerationScan()
//      payloaded = true
    }
    oldChunkPosition.set(currentChunkPosition)
  }

  fun getChunkPosition(): Vector2ic = currentChunkPosition

  override fun onTick(delta: Float) {
    super.onTick(delta)
    val jump = if (onGround && positionBuffer.y != 0) 0.5525f else 0f
    val cameraYaw = camera.getYaw()
    val forwardBuffer = (positionBuffer.z.toFloat() / 10f)
    val sidewaysBuffer = (positionBuffer.x.toFloat() / 10f)
    addVelocity(
      ((sin(-cameraYaw) * forwardBuffer) + (sin(-cameraYaw + (PI / 2.0f)) * sidewaysBuffer)).toFloat(),
      jump,
      ((cos(cameraYaw) * forwardBuffer) + (cos(cameraYaw - (PI / 2.0f)) * sidewaysBuffer)).toFloat()
    )
  }

  internal fun glueCamera() {
    camera.setPosition(interpolationPosition.x(), interpolationPosition.y() + eyeHeight, + interpolationPosition.z())
  }

  internal fun doClientControls() {
    positionBuffer.z = when {
      keyboard.isDown(GLFW.GLFW_KEY_LEFT_CONTROL) -> when {
        keyboard.isDown(GLFW.GLFW_KEY_W) -> -2
        keyboard.isDown(GLFW.GLFW_KEY_S) -> 2
        else -> 0
      }
      keyboard.isDown(GLFW.GLFW_KEY_W) -> -1
      keyboard.isDown(GLFW.GLFW_KEY_S) -> 1
      else -> 0
    }
    positionBuffer.x = when {
      keyboard.isDown(GLFW.GLFW_KEY_A) -> -1
      keyboard.isDown(GLFW.GLFW_KEY_D) -> 1
      else -> 0
    }
    positionBuffer.y = when {
      keyboard.isDown(GLFW.GLFW_KEY_LEFT_SHIFT) -> -1
      keyboard.isDown(GLFW.GLFW_KEY_SPACE) -> 1
      else -> 0
    }
  }

}