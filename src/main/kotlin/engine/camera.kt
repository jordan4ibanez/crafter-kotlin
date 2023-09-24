package engine

import org.joml.Math
import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.joml.Vector3f


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




}