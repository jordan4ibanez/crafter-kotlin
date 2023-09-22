package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.system.MemoryUtil.NULL

object window {
  internal var pointer: Long = NULL
  private const val WINDOW_TITLE_BASE = "Crafter"
  internal val frameBufferSize = Vector2i(0,0)
  internal val position = Vector2i(0,0)

  internal fun getTitleBase(): String {
    return WINDOW_TITLE_BASE
  }

  fun getFrameBufferSize(): Vector2ic {
    return frameBufferSize
  }




}