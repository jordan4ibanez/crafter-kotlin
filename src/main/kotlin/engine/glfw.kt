package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.system.MemoryUtil.NULL

object glfw {

  // This object is abstracting away complex implementation details away from the window object.

  private val monitorSize = Vector2i(0,0)

  fun initialize() {

    // A simple way to stop this from being called multiple times.
    if (window.pointer != NULL) {
      throw RuntimeException("GLFW: Attempted to initialize GLFW with active window.")
    }

    // Set the error callback for GLFW to funnel into JRE System Error output.
    GLFWErrorCallback.createPrint(System.err).set()

    // Attempt to initialize GLFW.
    if (!glfwInit()) {
      throw RuntimeException("GLFW: Failed to initialize GLFW.")
    }

    // Initialize the default window hints.
    glfwDefaultWindowHints();

    // Enable OpenGL debug mode.
    glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)

    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    // We want a base of OpenGL 4.1, but also allow driver optimizations.
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)

    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)

    // Automatically updates the window object pointer.
    constructWindow()

    glfwMakeContextCurrent(window.pointer)

    glfwSwapInterval(1)

    glfwShowWindow(window.pointer)

  }

  private fun constructWindow() {

    // This function is talking to the window object.

    val (monitorSizeX, monitorSizeY) = getMonitorSize().destructure()
    val (windowSizeX, windowSizeY) = arrayOf(monitorSizeX / 2, monitorSizeY / 2)
    val (windowPosX, windowPosY) = arrayOf((monitorSizeX - windowSizeX) / 2, (monitorSizeY - windowSizeY) / 2)
    window.pointer = glfwCreateWindow(windowSizeX, windowSizeY, window.getTitleBase(), NULL, NULL)

    // Now if this gets called, we have a serious problem.
    if (window.pointer == NULL) {
      throw RuntimeException("GLFW: Failed to create GLFW window.")
    }

    glfwSetFramebufferSizeCallback(window.pointer) { _, width, height ->
      println("Framebuffer was resized to: $width, $height")
      glViewport(0, 0, width, height)
      window.frameBufferSize.set(width, height)
    }

    glfwSetWindowPosCallback(window.pointer) { _, positionX, positionY ->
      println("Window was moved to: $positionX, $positionY")
      window.position.set(positionX, positionY)
    }

    glfwSetWindowPos(window.pointer, windowPosX, windowPosY)
  }

  fun getMonitorSize(): Vector2ic {
    val mainMonitor = glfwGetPrimaryMonitor()
    val videoMode = glfwGetVideoMode(mainMonitor) ?: throw RuntimeException("GLFW: Failed to get monitor video mode.")

    // We only want one of this object. Share it as READ-ONLY throughout the entire program.
    return monitorSize.set(videoMode.width(), videoMode.height())
  }

  fun destroy() {
    glfwFreeCallbacks(window.pointer)
    glfwDestroyWindow(window.pointer)
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
  }

}