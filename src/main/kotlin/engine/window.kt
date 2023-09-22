package engine

import org.joml.Vector2i
import org.joml.Vector2ic
import org.lwjgl.system.MemoryUtil.NULL

object window {
  private var pointer: Long = NULL
  private const val WINDOW_TITLE_BASE = "Crafter"
  internal val frameBufferSize = Vector2i(0,0)

  internal fun getPointer(): Long {
    return pointer
  }

  internal fun setPointer(pointer: Long) {
    this.pointer = pointer
  }

  internal fun getTitleBase(): String {
    return WINDOW_TITLE_BASE
  }

  fun getFrameBufferSize(): Vector2ic {
    return frameBufferSize
  }


}