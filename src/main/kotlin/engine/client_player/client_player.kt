package engine.client_player

import engine.Player
import engine.camera.camera
import engine.entity
import engine.keyboard
import engine.world
import org.joml.Math.*
import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.glfw.GLFW
import kotlin.math.floor

object clientPlayer : Player(Vector3f(0f, 110f, 0f), "singleplayer") {

  //? note: this will create a bug that if you're sitting at the exact corner of the world, it doesn't auto scan. Who cares.
  private val oldChunkPosition = Vector2i(Int.MAX_VALUE, Int.MAX_VALUE)
  private val currentChunkPosition = Vector2i()
  private val positionBuffer = Vector2i()
  private val chunkWidth = world.getChunkWidthFloat()
  private val chunkDepth = world.getChunkDepthFloat()
  private var jumping = false
  private var sneaking = false

  fun initialize() {
    // Automatically add in the client player into players.
    entity.addPlayer(this)
  }

  override fun setPosition(newPosition: Vector3fc) {
    oldPosition.set(position)
    position.set(newPosition)
    val x: Int = floor(newPosition.x() / chunkWidth).toInt()
    val z: Int = floor(newPosition.z() / chunkDepth).toInt()
    currentChunkPosition.set(x, z)
    if (currentChunkPosition != oldChunkPosition /*&& !payloaded*/) world.cleanAndGenerationScan()
    oldChunkPosition.set(currentChunkPosition)
    interpolationTimer = 0f
  }

  fun getChunkPosition(): Vector2ic = currentChunkPosition

  override fun onTick(delta: Float) {
    super.onTick(delta)
    val cameraYaw = camera.getYaw()
    val forwardBuffer = positionBuffer.y.toFloat()
    val sidewaysBuffer = positionBuffer.x.toFloat()

    // Running/walking.
    val speedGoal =
      if (sneaking) 0.175f else (if (abs(positionBuffer.x) > 1 || abs(positionBuffer.y) > 1) 0.35f else 0.25f)
    // Jumping/sneaking.
    val jump = if (onGround && jumping) 0.5f else 0f

    mobMove(
      ((sin(-cameraYaw) * forwardBuffer) + (sin(-cameraYaw + (PI / 2.0f)) * sidewaysBuffer)).toFloat(),
      ((cos(cameraYaw) * forwardBuffer) + (cos(cameraYaw - (PI / 2.0f)) * sidewaysBuffer)).toFloat(),
      speedGoal
    )

    if (jump != 0f) addVelocity(0f, jump, 0f)
  }

  internal fun glueCamera() {
    camera.setPosition(interpolationPosition.x(), interpolationPosition.y() + eyeHeight, +interpolationPosition.z())
  }

  internal fun doClientControls() {
    positionBuffer.y = when {
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

    sneaking = keyboard.isDown(GLFW.GLFW_KEY_LEFT_SHIFT)
    jumping = keyboard.isDown(GLFW.GLFW_KEY_SPACE)
  }
}