package engine

import org.lwjgl.glfw.GLFWErrorCallback

fun initializeGLFW() {

  //* GLFW
  GLFWErrorCallback.createPrint(System.err).set()


}
