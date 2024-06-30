package engine.glfw

import engine.joml_bolt_ons.destructure
import engine.keyboard.Keyboard
import engine.mouse.Mouse
import engine.window.window
import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.system.MemoryUtil.NULL

object Glfw {

  // This object is abstracting away complex implementation details away from the window object.

  private val monitorSize = Vector2i(0, 0)

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

    // Puts all callback initializations into one function scope.
    constructCallbacks()

    glfwMakeContextCurrent(window.pointer)
    glfwSwapInterval(1)
    glfwShowWindow(window.pointer)

    // Create the initial framebuffer size in window object.
    run {
      val x = IntArray(1)
      val y = IntArray(1)
      glfwGetFramebufferSize(window.pointer, x, y)
      window.frameBufferSize.set(x[0], y[0])
    }

    // Mouse raw motion support
    if (glfwRawMouseMotionSupported()) {
      glfwSetInputMode(window.pointer, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE)
    }
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

    window.setPosition(windowPosX, windowPosY)
  }


  private fun constructCallbacks() {

    //note: window

    glfwSetFramebufferSizeCallback(window.pointer) { _, width, height ->
      println("Framebuffer was resized to: $width, $height")
      glViewport(0, 0, width, height)
      window.frameBufferSize.set(width, height)
    }

    glfwSetWindowPosCallback(window.pointer) { _, positionX, positionY ->
      println("Window was moved to: $positionX, $positionY")
      window.position.set(positionX, positionY)
    }

    //note: keyboard

    glfwSetCharCallback(window.pointer) { _, codePoint ->
      Keyboard.lastKey = codePoint.toChar()
    }

    glfwSetKeyCallback(window.pointer) { _, key, scancode, action, mods ->
      if (action == GLFW_PRESS || action == GLFW_REPEAT) {
        Keyboard.setMemory(key)
        Keyboard.setCurrent(key, true)
        Keyboard.memoryFlush.add(key)
      } else if (action == GLFW_RELEASE) {
        //todo: this needs to be tested, these might need to be flipped
        Keyboard.setCurrent(key, false)
        Keyboard.setMemory(key)
      }
    }

    // Note: Mouse.
    glfwSetCursorPosCallback(window.pointer) { _, posX, posY ->
      Mouse.position.set(posX, posY)
    }

    glfwSetCursorEnterCallback(window.pointer) { _, entered ->
      // Only reset to -1 when mouse leaves.
      if (!entered) {
        println("mouse: resetting position to -1, -1")
        Mouse.position.set(-1f, -1f)
      }
    }
  }

  fun getMonitorSize(): Vector2ic {
    val mainMonitor = glfwGetPrimaryMonitor()
    val videoMode = glfwGetVideoMode(mainMonitor) ?: throw RuntimeException("GLFW: Failed to get monitor video mode.")
    return monitorSize.set(videoMode.width(), videoMode.height())
  }

  fun destroy() {
    glfwFreeCallbacks(window.pointer)
    glfwDestroyWindow(window.pointer)
    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
  }
}