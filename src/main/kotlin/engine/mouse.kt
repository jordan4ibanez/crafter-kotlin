package engine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.glfw.GLFW.*

object mouse {
  internal val position = Vector2f(-1f, -1f)
  private val oldPosition = Vector2f(-1f, -1f)
  private val delta = Vector2f(0f, 0f)

  private var leftClick = false
  private var leftHeld = false
  private var leftWasHeld = false

  private var rightClick = false
  private var rightHeld = false
  private var rightWasHeld = false

  private var needsDeltaReset = true

  internal fun poll() {
    calculateDeltaWhenCaptured()

    val leftButtonState = glfwGetMouseButton(window.pointer, GLFW_MOUSE_BUTTON_LEFT)
    val rightButtonState = glfwGetMouseButton(window.pointer, GLFW_MOUSE_BUTTON_RIGHT)

    if (leftButtonState == GLFW_PRESS) {
      leftHeld = true
      leftClick = !leftWasHeld
      leftWasHeld = true
    } else if (leftButtonState == GLFW_RELEASE) {
      leftClick = false
      leftHeld = false
      leftWasHeld = false
    }

    if (rightButtonState == GLFW_PRESS) {
      rightHeld = true
      rightClick = !rightWasHeld
      rightWasHeld = true
    } else if (rightButtonState == GLFW_RELEASE) {
      rightClick = false
      rightHeld = false
      rightWasHeld = false
    }
  }

  private fun calculateDeltaWhenCaptured() {
    if (isCaptured()) {
      if (!needsDeltaReset) {
        position.sub(oldPosition, delta)
      } else {
        doReset()
      }
      oldPosition.set(position)
    }
  }

  fun leftClick(): Boolean = leftClick
  fun rightClick(): Boolean = rightClick
  fun leftHeld(): Boolean = leftHeld
  fun rightHeld(): Boolean = rightHeld
  fun getPosition(): Vector2fc = position
  fun getDelta(): Vector2fc = delta



  internal fun capture() {
    glfwSetInputMode(window.pointer, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    needsDeltaReset = true
  }

  internal fun release() {
    glfwSetInputMode(window.pointer, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
    needsDeltaReset = true
  }

  internal fun isCaptured(): Boolean = glfwGetInputMode(window.pointer, GLFW_CURSOR) == GLFW_CURSOR_DISABLED

  private fun doReset() {
    delta.zero()
    //! FIXME: move this to the center of the window instead of the top left.
    glfwSetCursorPos(window.pointer, 0.0, 0.0)
    needsDeltaReset = false
  }

}