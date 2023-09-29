package engine

import kotlinx.coroutines.*
import org.joml.Math
import org.joml.Vector2i
import org.joml.Vector2ic
import java.util.concurrent.ConcurrentLinkedQueue

/*
This is a data oriented approach to the mess that was in Java.
*/

private const val WIDTH = 16
private const val HEIGHT = 128
private const val DEPTH = 16
private const val yStride = WIDTH * DEPTH
private const val ARRAY_SIZE = WIDTH * HEIGHT * DEPTH

private val data = HashMap<Vector2ic, IntArray>()

// Input into chunk generator goes into here.
private val generationInput = ConcurrentLinkedQueue<Vector2ic>()
// Output from chunk generator coroutines goes into here.
private val generationOutput = ConcurrentLinkedQueue<IntArray>()

@OptIn(DelicateCoroutinesApi::class)
internal fun disperseChunkGenerators() {
  //note: Wrapper function to make implementation cleaner.
  // Shoot and forget.

  // If there's nothing to be done, do nothing.
  if (generationInput.isEmpty()) return
  GlobalScope.launch {genChunk() }
}

fun genChunk() {
  //note: Async double check.
  if (generationInput.isEmpty()) return
  val position = generationInput.remove()!!

  //fixme: placeholder
  val grass = 1
  val dirt  = 2
  val stone = 3


}