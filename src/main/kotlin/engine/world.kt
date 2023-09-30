package engine

import kotlinx.coroutines.*
import org.joml.Math
import org.joml.Math.random
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

private var seed = 123_456_789

private val data = HashMap<Vector2ic, IntArray>()

// Input into chunk generator goes into here.
private val generationInput = ConcurrentLinkedQueue<Vector2ic>()
// Output from chunk generator coroutines goes into here.
private val generationOutput = ConcurrentLinkedQueue<Pair<Vector2ic, IntArray>>()

//note: Then it will go into hashmap<Vector2ic, IntArray>

@OptIn(DelicateCoroutinesApi::class)
internal fun disperseChunkGenerators() {
  //note: Wrapper function to make implementation cleaner.
  // Shoot and forget.

  // If there's nothing to be done, do nothing.
//  if (generationInput.isEmpty()) return
  GlobalScope.launch {genChunk() }
}

private fun genChunk() {
  //note: Async double check.

  //Fixme: Remember to re-enable this!
  // if (generationInput.isEmpty()) return
  // val position = generationInput.remove()!!

  //fixme: placeholder
  val grass = 1
  val dirt  = 2
  val stone = 3

  // fixme: Needs to be passed in val
  val xOffset = 0
  val zOffset = 0

  val biomeFrequency = 0.1f
  val biomeScale = 1f
  val biomeBaseHeight = 60

  val noise = Noise(seed)

  noise.setFrequency(biomeFrequency)

  //note: At the moment, this is a controlled memory leak.
  // A durability test.


  val dataArray = IntArray(WIDTH * HEIGHT * DEPTH)
  val index = 0

//  println(noise.getNoise(random().toFloat() * 123f, random().toFloat() * 123f))

  for (x in 0 until WIDTH) {
    for (z in 0 until DEPTH) {

      //note: +0.5 because the output is -0.5 to 0.5
      val calculatedNoise = noise.getSimplex(x + xOffset.toFloat(), z + zOffset.toFloat()) + 0.5f

      val height = (calculatedNoise * biomeScale) + biomeBaseHeight

      for (y in 0 until HEIGHT) {

        // Starts off as air
        var id = 0

        if (y < height - 6) {
          id = stone
        } else if (y < height - 1) {
          id = dirt
        } else if (y < height) {
          id = grass
        }

        //todo:
        // Needs to use functional chunk api to assign into array
        // for now, this is a test
        dataArray[index] = id

      }
    }
  }

  //Note: this will crash.
  generationOutput.add(Pair(Vector2i(1,2), dataArray))

  println("size: ${generationOutput.size}")

}


private fun process() {
  //todo:
  // This will collect
}