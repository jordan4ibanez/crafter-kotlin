package engine

import kotlinx.coroutines.*
import org.joml.Math
import org.joml.Math.random
import org.joml.Vector2i
import org.joml.Vector2ic
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

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

fun generateChunk(x: Int, y: Int) {
  val key = Vector2i(x, y)
  if (data.containsKey(key) || generationInput.contains(key)) {
    println("Discarding generation $x, $y")
    return
  }
  generationInput.add(key)
}

@OptIn(DelicateCoroutinesApi::class)
internal fun disperseChunkGenerators() {
  //note: Wrapper function to make implementation cleaner.
  // Shoot and forget. More like a machine gun.

  // If there's nothing to be done, do nothing.
  var counter = 0
  while (!generationInput.isEmpty()) {
    GlobalScope.launch { genChunk() }
    // fixme: Needs a setting like maxChunkGensPerFrame or something
    counter++
    if (counter >= 10) {
      break
    }
  }
  counter = 0
  while (!generationOutput.isEmpty()) {
    GlobalScope.launch { processChunks() }
    // fixme: Needs a setting like maxChunkProcessesPerFrame or something
    counter++
    if (counter >= 10) {
      break
    }
  }
}

private fun genChunk() {
  //note: Async double check.
   if (generationInput.isEmpty()) return

   val (xOffset, zOffset) = generationInput.remove()!!.destructure()

  println("Generating: $xOffset, $zOffset")

  //fixme: placeholder
  val grass = 1
  val dirt  = 2
  val stone = 3

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

  generationOutput.add(Pair(Vector2i(1,2), dataArray))
}


private fun processChunks() {
  if (generationOutput.isEmpty()) return

  val (position, chunkData) = generationOutput.remove()!!

  data[position] = chunkData

  val dataClone = chunkData.clone()

  //fixme: needs a functional safe getter for the data hashmap
  //get -x +x +z -z clones

  //todo: process mesh here

  // done
}
