package engine

import kotlinx.coroutines.*
import org.joml.Math
import org.joml.Math.random
import org.joml.Vector2i
import org.joml.Vector2ic
import java.util.concurrent.ConcurrentHashMap
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

private val data = ConcurrentHashMap<Vector2ic, IntArray>()

// Input into chunk generator goes into here.
private val generationInput = ConcurrentLinkedQueue<Vector2ic>()
// Output from chunk generator coroutines goes into here.
private val generationOutput = ConcurrentLinkedQueue<Pair<Vector2ic, IntArray>>()

// note: API begins here

fun generateChunk(x: Int, y: Int) {
  val key = Vector2i(x, y)
  if (data.containsKey(key) || generationInput.contains(key)) {
    println("Discarding generation $x, $y")
    return
  }
  generationInput.add(key)
}

fun chunkExists(posX: Int, posZ: Int): Boolean {
  return data.contains(Vector2i(posX, posZ))
}

// I lub extension functions
fun Int.blockBits(block: Int): String {
  val builder = StringBuilder()
  for (i in 31 downTo 0) {
    if ((i + 1) % 4 == 0) {
      builder.append("|")
    }
    builder.append(if ((block and (1 shl i)) == 0) "0" else "1")
  }
  return builder.toString()
}

fun Int.getBlockID(): Int {
  return this ushr 16
}

fun Int.getBlockLight(): Int {
  return this shl 16 ushr 28
}

fun Int.getBlockState(): Int {
  return this shl 20 ushr 28
}

infix fun Int.setBlockID(newID: Int): Int {
  if (!(0..65535).contains(newID)) throw RuntimeException("passed in value larger than 16 bits to set id")
  return combine(newID.shiftBlock(),this.parseBlockLight(),this.parseBlockState())
}

infix fun Int.setBlockLight(newLight: Int): Int {
  if (!(0..15).contains(newLight)) throw RuntimeException("passed in value larger than 4 bits to set light")
  return combine(this.parseBlockID(), newLight.shiftLight(), this.parseBlockState())
}

infix fun Int.setBlockState(newState: Int): Int {
  if (!(0..15).contains(newState)) throw RuntimeException("passed in value larger than 4 bits to set state")
  return combine(this.parseBlockID(), this.parseBlockLight(), newState.shiftState())
}

fun poop(cool: Int): Int {
  return cool setBlockState 1 setBlockID 123 setBlockLight 14
}

// raw parsers, do not give out true value, they are bit container locks essentially.
private fun Int.parseBlockID(): Int {
  return this ushr 16 shl 16
}
private fun Int.parseBlockLight(): Int {
  val i = this shl 16 ushr 16
  return i ushr 12 shl 12
}
private fun Int.parseBlockState(): Int {
  val i = this shl 20 ushr 20
  return i ushr 8 shl 8
}


// shifters & combiner
private fun Int.shiftBlock(): Int {
  return this shl 16
}
private fun Int.shiftLight(): Int {
  return this shl 12
}
private fun Int.shiftState(): Int {
  return this shl 8
}
private fun Int.combine(blockID: Int, light: Int, state: Int): Int {
  return blockID xor light xor state
}


// note: Internal begins here

private fun safetGet(posX: Int, posZ: Int): IntArray {
  return data[Vector2i(posX, posZ)] ?: throw RuntimeException("world: tried to access nonexistent chunk: $posX, $posZ")
}

private fun safeGetDeconstruct(posX: Int, posZ: Int): Pair<Boolean, IntArray> {
  return if (chunkExists(posX, posZ)) {
    Pair(true, safetGet(posX, posZ).clone())
  } else {
    Pair(false, IntArray(0))
  }
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

//  println("Generating: $xOffset, $zOffset")

  //fixme: placeholder
  val grass = 1
  val dirt  = 2
  val stone = 3

  val biomeFrequency = 0.1f
  val biomeScale = 1f
  val biomeBaseHeight = 60

  val noise = Noise(seed)

  noise.setFrequency(biomeFrequency)

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

  // Separate internal pointer
  val dataClone = chunkData.clone()

  buildChunkMesh(position.x(), position.y(), dataClone)

  // done
}

private fun buildChunkMesh(posX: Int, posZ: Int, chunkData: IntArray) {

  val (leftExists, left) = safeGetDeconstruct(posX - 1, posZ)
  val (rightExists, right) = safeGetDeconstruct(posX + 1, posZ)
  val (backExists, back) = safeGetDeconstruct(posX, posZ + 1)
  val (frontExists, front) = safeGetDeconstruct(posX, posZ - 1)

  println("buildChunkMesh is running")
}
