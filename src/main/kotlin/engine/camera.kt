package engine

import org.joml.Math
import org.joml.Math.*
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.windows.INPUT


object camera {

  private var renderDistance = 16

  private var sensitivity = 1f / 500f
  private var FOV: Float = toRadians(60f)
  private var zNear = 0.1f
  private var zFar = (renderDistance * world.getChunkWidth()).toFloat() * 1.5f
  private val cameraMatrix = Matrix4f()
  private val objectMatrix = Matrix4f()
  private val guiCameraMatrix = Matrix4f()
  private val guiObjectMatrix = Matrix4f()
  private val position = Vector3f(0f,78f,0f)
  private val rotation = Vector3f(0f,0f,0f)

  private val workerVector3f = Vector3f()
  private val workerVector3f2 = Vector3f()

  // Movement vals. fixme: Why the HECK are there so many variables to do basic movement??
  private val inputMovement = Vector3f()
  private val cameraMovementX = Vector3f()
  private val cameraMovementY = Vector3f()
  private val cameraMovementZ = Vector3f()
  private val finalCameraMovement = Vector3f()
  private val newCameraRotation = Vector3f()
  private val newCameraPosition = Vector3f()
  private val cameraDelta = Vector3f()


  fun updateCameraMatrix() {
    cameraMatrix
      .identity()
      .perspective(FOV, window.getAspectRatio(), zNear, zFar)
      .rotateX(rotation.x)
      .rotateY(rotation.y)

    shader.setUniform("cameraMatrix", cameraMatrix)
  }

  fun getZNear(): Float = zNear
  fun getZFar(): Float = zFar


  fun setObjectMatrix(position: Vector3fc) {
    setObjectMatrix(position, workerVector3f2.set(0f,0f,0f))
  }

  fun setObjectMatrix(position: Vector3fc, rotation:Vector3fc) {
    setObjectMatrix(position, rotation, workerVector3f.set(1f,1f,1f))
  }

  fun setObjectMatrix(position: Vector3fc, rotation: Vector3fc, scale: Vector3fc) {
    objectMatrix
      .identity()
      .translate(
        position.x() - this.position.x(),
        position.y() - this.position.y(),
        position.z() - this.position.z())
      .rotateY(-rotation.y())
      .rotateX(-rotation.x())
      .rotateZ(-rotation.z())
      .scale(scale)

    shader.setUniform("objectMatrix", objectMatrix)
  }

  fun updateGUICameraMatrix() {
    val (width, height) = window.getFrameBufferSize().destructure()

    guiCameraMatrix
      .identity()
      .setOrtho2D(0f, width.toFloat(), height.toFloat(), 0f)

    shader.setUniform("cameraMatrix", guiCameraMatrix)
  }


  fun setGUIObjectMatrix(posX: Float, posY: Float) {
    setGUIObjectMatrix(posX, posY, 1f, 1f)
  }

  fun setGUIObjectMatrix(posX: Float, posY: Float, scaleX: Float, scaleY: Float) {
    guiObjectMatrix
      .identity()
      .translate(posX, posY, 0f)
      .scale(scaleX, scaleY, 1f)

    shader.setUniform("objectMatrix", guiObjectMatrix)
  }

  fun getCameraMatrix(): Matrix4fc = cameraMatrix
  fun getObjectMatrix(): Matrix4fc = objectMatrix

  fun getHorizontalDirection(yaw: Float): Vector3fc {
    workerVector3f.zero()
    workerVector3f.x = sin(-yaw)
    workerVector3f.z = cos(yaw)
    return workerVector3f
  }

  fun getRenderDistance(): Int = renderDistance

  fun setPosition(newPosition: Vector3fc) {
    position.set(newPosition)
  }

  fun getPosition(): Vector3fc = position
  fun yawToLeft(yaw: Float): Float = yaw - (PI / 2F).toFloat()

  private fun doMouseInputCameraRotation() {
    val mouseDelta = mouse.getDelta()
    cameraDelta.set(mouseDelta.y(), mouseDelta.x(), 0f).mul(sensitivity)
    rotation.add(cameraDelta, newCameraRotation)
    rotation.set(newCameraRotation)
  }

  fun freeCam() {

    //! FIXME: this is an overcomplicated mess.

    doMouseInputCameraRotation()

    inputMovement.zero()

    if (keyboard.isDown(GLFW_KEY_W)) inputMovement.z -= 1f
    if (keyboard.isDown(GLFW_KEY_S)) inputMovement.z += 1f
    if (keyboard.isDown(GLFW_KEY_A)) inputMovement.x -= 1f
    if (keyboard.isDown(GLFW_KEY_D)) inputMovement.x += 1f
    if (keyboard.isDown(GLFW_KEY_SPACE)) inputMovement.y += 1f
    if (keyboard.isDown(GLFW_KEY_LEFT_SHIFT)) inputMovement.y -= 1f

    val yaw = newCameraRotation.y
    val movementDelta = getDelta() * 150f

    // Layered fixme: Why is this layered??
    cameraMovementX.zero()
    cameraMovementY.zero()
    cameraMovementZ.zero()
    finalCameraMovement.zero()

    cameraMovementX.set(getHorizontalDirection(yawToLeft(yaw))).mul(inputMovement.x)
    cameraMovementY.set(0f, inputMovement.y, 0f)
    cameraMovementZ.set(getHorizontalDirection(yaw)).mul(inputMovement.z)

    finalCameraMovement.set(cameraMovementX.add(cameraMovementY).add(cameraMovementZ)).mul(movementDelta)

    val cameraPosition = getPosition()
    cameraPosition.add(finalCameraMovement, newCameraPosition)

    setPosition(newCameraPosition)
    newCameraPosition.y -= clientPlayer.eyeHeight
    clientPlayer.setPosition(newCameraPosition)
  }
}