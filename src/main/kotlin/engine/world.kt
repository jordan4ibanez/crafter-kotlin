package engine

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

internal suspend fun disperseChunkGenerators() {

}