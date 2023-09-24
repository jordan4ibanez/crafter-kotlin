package engine

import org.joml.Math
import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc


object camera {
  private var sensitivity = 500f
  private var FOV: Float = toRadians(60f)
  private var zNear = 0.1f
  private var zFar = 1000f
  private val cameraMatrix = Matrix4f()
  private val objectMatrix = Matrix4f()
  private val guiCameraMatrix = Matrix4f()
  private val guiObjectMatrix = Matrix4f()
  private val position = Vector3f(0f,0f,0f)
  private val rotation = Vector3f(0f,0f,0f)

  fun updateCameraMatrix() {
    cameraMatrix
      .identity()
      .perspective(FOV, window.getAspectRatio(), zNear, zFar)
      .rotateX(rotation.x)
      .rotateY(rotation.y)

    shader.setUniform("cameraMatrix", cameraMatrix)
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

  // Todo: Put the rest of the functionality in here.
}