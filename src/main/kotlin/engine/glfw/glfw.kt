package engine.glfw

import engine.destructure
import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.system.MemoryUtil.NULL

object glfw {

  private val monitorSize = Vector2i()
  private var windowPointer: Long = NULL

  fun initialize() {

    // A simple way to stop this from being called multiple times.
    if (windowPointer != NULL) {
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

    run {
      val (sizeX, sizeY) = getMonitorSize().destructure()
      windowPointer = glfwCreateWindow(sizeX / 2, sizeY / 2, "Whatever the game is called", NULL, NULL)
    }

    // Now if this gets called, we have a serious problem.
    if (windowPointer == NULL) {
      throw RuntimeException("GLFW: Failed to create GLFW window.")
    }

    glfwSetFramebufferSizeCallback(windowPointer) { _, width, height ->
      println("Window was resized to: $width, $height")
      glViewport(0, 0, width, height)
    }



  }

  fun getMonitorSize(): Vector2ic {
    val mainMonitor = glfwGetPrimaryMonitor()
    val videoMode = glfwGetVideoMode(mainMonitor) ?: throw RuntimeException("GLFW: Failed to get monitor video mode.")

    // We only want one of this object. Share it as READ-ONLY throughout the entire program.
    return monitorSize.set(videoMode.width(), videoMode.height())
  }


}