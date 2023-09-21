package engine

import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS
import org.lwjgl.opengl.GLUtil.setupDebugMessageCallback
import org.lwjgl.system.Callback

object gl {

  private lateinit var debugCallback: Callback

  fun initialize() {
    createCapabilities()
    println(glGetString(GL_VERSION))

    glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)

    // If System.err is uncommented, when there's an OpenGL error, this will completely stop the program.
    debugCallback = setupDebugMessageCallback(/*System.err*/) ?: throw RuntimeException("GL: Failed to initialize debug callback.")

    glDepthMask(true)
    glEnable(GL_DEPTH_TEST)
    glDepthFunc(GL_LESS)
//    glDepthRange(camera near, camera far)

    val enableCulling = false

    if (enableCulling) {
      glEnable(GL_CULL_FACE)
    } else {
      glDisable(GL_CULL_FACE)
    }


  }
}