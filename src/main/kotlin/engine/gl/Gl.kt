package engine.gl

import engine.camera.Camera
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.GL14.*
import org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS
import org.lwjgl.opengl.GLCapabilities
import org.lwjgl.opengl.GLUtil.setupDebugMessageCallback
import org.lwjgl.system.Callback

object Gl {

  private lateinit var debugCallback: Callback
  private lateinit var capabilities: GLCapabilities

  fun initialize() {

    capabilities = createCapabilities()

    println(glGetString(GL_VERSION))

    glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)

    // If System.err is uncommented, when there's an OpenGL error, this will completely stop the program.
    debugCallback =
      setupDebugMessageCallback(/*System.err*/) ?: throw RuntimeException("GL: Failed to initialize debug callback.")

    glDepthMask(true)
    glEnable(GL_DEPTH_TEST)
    glDepthFunc(GL_LESS)

    val enableAlphaBlend = false

    if (enableAlphaBlend) {
      glEnable(GL_BLEND);
      glBlendEquation(GL_FUNC_ADD);
      glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
      glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
    }

    //todo: this has to be implemented before it's used
    glDepthRange(Camera.getZNear().toDouble(), Camera.getZFar().toDouble())

    val enableCulling = true

    if (enableCulling) {
      glEnable(GL_CULL_FACE)
    } else {
      glDisable(GL_CULL_FACE)
    }
  }
}