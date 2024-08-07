package engine.window

import engine.delta_time.DeltaTime
import engine.keyboard.Keyboard
import engine.mouse.Mouse
import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

object Window {

  private const val WINDOW_TITLE_BASE = "Crafter"

  internal var pointer: Long = NULL
  internal val frameBufferSize = Vector2i(0, 0)
  internal val position = Vector2i(0, 0)
  private val clearColor = Vector3f(0f, 0f, 0f)

  internal fun getTitleBase(): String {
    return WINDOW_TITLE_BASE
  }

  fun setTitle(newTitle: String) {
    glfwSetWindowTitle(pointer, "$WINDOW_TITLE_BASE | $newTitle")
  }

  fun getFrameBufferSize(): Vector2ic {
    return frameBufferSize
  }

  fun getPosition(): Vector2ic {
    return position
  }

  fun setPosition(x: Int, y: Int) {
    glfwSetWindowPos(pointer, x, y)
  }

  fun shouldClose(): Boolean {
    return glfwWindowShouldClose(pointer)
  }

  fun setClearColor(r: Float, g: Float, b: Float) {
    clearColor.set(r, g, b)
    glClearColor(clearColor.x(), clearColor.y(), clearColor.z(), 1f)
  }

  fun setClearColor(scalar: Float) {
    clearColor.set(scalar)
    glClearColor(scalar, scalar, scalar, 1f)
  }

  fun setVsync(state: Boolean) {
    glfwSwapInterval(if (state) GLFW_TRUE else GLFW_FALSE)
  }

  fun getClearColor(): Vector3fc {
    return clearColor
  }

  fun getAspectRatio(): Float {
    return frameBufferSize.x().toFloat() / frameBufferSize.y().toFloat()
  }

  fun isMaximized(): Boolean {
    return glfwGetWindowAttrib(pointer, GLFW_MAXIMIZED) == GLFW_TRUE
  }

  fun maximize() {
    glfwMaximizeWindow(pointer)
  }

  fun unMaximize() {
    glfwRestoreWindow(pointer)
  }

  fun isFocused(): Boolean {
    return glfwGetWindowAttrib(pointer, GLFW_FOCUSED) == GLFW_TRUE
  }

  fun update() {
    Keyboard.pollMemory()
    Mouse.poll()
    DeltaTime.calculate()
    glfwPollEvents()
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
  }

  fun swapBuffers() {
    glfwSwapBuffers(pointer)
  }

  fun close() {
    glfwSetWindowShouldClose(pointer, true)
  }
}