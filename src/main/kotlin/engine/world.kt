package engine

import kotlinx.coroutines.*
import org.joml.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/*
This is a data oriented approach to the mess that was in Java.
*/

private const val WIDTH = 16
private const val HEIGHT = 128
private const val DEPTH = 16
private const val yStride = WIDTH * DEPTH
private const val ARRAY_SIZE = WIDTH * HEIGHT * DEPTH

private const val MESH_ARRAY_SIZE = 8

private var seed = 123_456_789

private const val MAX_CHUNK_GENS_PER_FRAME = 10
private const val MAX_CHUNK_MESH_PROCS_PER_FRAME = 10
private const val MAX_CHUNK_PROCS_PER_FRAME = 10

// Chunk block data
//! todo: upgrade to LongArray! This will allow either 24 bit or 32 bit limit for chunks!
private val data = ConcurrentHashMap<Vector2ic, IntArray>()

private val meshIDs = HashMap<Vector2ic, Array<String>>()

// Input into chunk generator goes into here.
private val dataGenerationInput = ConcurrentLinkedQueue<Vector2ic>()
// Output from chunk generator coroutines goes into here.
private val dataGenerationOutput = ConcurrentLinkedQueue<Pair<Vector2ic, IntArray>>()

// Input into the chunk mesh generator goes into here.
private val meshGenerationInput = ConcurrentLinkedQueue<Vector3ic>()
// Output from the chunk mesh generator goes into here.
private val meshGenerationOutput = ConcurrentLinkedQueue<Pair<Vector3ic, ChunkMesh>>()

// note: API begins here

fun generateChunk(x: Int, y: Int) {
  val key = Vector2i(x, y)
  if (data.containsKey(key) || dataGenerationInput.contains(key)) {
    println("Discarding generation $x, $y")
    return
  }
  dataGenerationInput.add(key)
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

fun posToIndex(x: Int, y: Int, z: Int): Int {
  return (y * yStride) + (z * DEPTH) + x;
}
fun posToIndex(pos: Vector3ic): Int {
  return (pos.y() * yStride) + (pos.z() * DEPTH) + pos.x()
}

fun indexToPos(i: Int): Vector3ic {
  return Vector3i(
    i % WIDTH,
    (i / yStride) % HEIGHT,
    (i / DEPTH) % DEPTH
  )
}


// note: Internal begins here

private fun safetGetData(posX: Int, posZ: Int): IntArray {
  return data[Vector2i(posX, posZ)] ?: throw RuntimeException("world: tried to access nonexistent chunk: $posX, $posZ")
}

private fun safeGetDataDeconstruct(posX: Int, posZ: Int): Pair<Boolean, IntArray> {
  return if (chunkExists(posX, posZ)) {
    Pair(true, safetGetData(posX, posZ).clone())
  } else {
    Pair(false, IntArray(0))
  }
}

@OptIn(DelicateCoroutinesApi::class)
internal fun disperseChunkGenerators() {
  //note: Wrapper function to make implementation cleaner.
  // Shoot and forget. More like a machine gun.

  // If there's nothing to be done, do nothing.

  for (i in 0 .. MAX_CHUNK_GENS_PER_FRAME) {
    if (dataGenerationInput.isEmpty()) break
    GlobalScope.launch { genChunk() }
  }

  for (i in 0 .. MAX_CHUNK_MESH_PROCS_PER_FRAME) {
    if (meshGenerationOutput.isEmpty()) break
    receiveChunkMeshes()
  }

  for (i in 0 .. MAX_CHUNK_PROCS_PER_FRAME) {
    if (dataGenerationOutput.isEmpty()) break
    GlobalScope.launch { processChunks() }
  }
}

private fun genChunk() {
  //note: Async double check.
  if (dataGenerationInput.isEmpty()) return

  val gotten: Vector2ic

  try {
    gotten = dataGenerationInput.remove()!!
  } catch (e: Exception) {
    println("genChunk: failed.")
    return
  }

  val (xOffset, zOffset) = gotten.destructure()

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

  val dataArray = IntArray(ARRAY_SIZE)

  for (x in 0 until WIDTH) {
    for (z in 0 until DEPTH) {

      //note: +0.5 because the output is -0.5 to 0.5
      val calculatedNoise = noise.getSimplex(x + xOffset.toFloat(), z + zOffset.toFloat()) + 0.5f

      val height = (calculatedNoise * biomeScale) + biomeBaseHeight

      for (y in 0 until HEIGHT) {

        val id = if (y < height - 6) {
          0 setBlockID stone setBlockLight 0
        } else if (y < height - 1) {
          0 setBlockID dirt setBlockLight 0
        } else if (y < height) {
          0 setBlockID grass setBlockLight 0
        } else {
          0 setBlockID 0 setBlockLight 15
        }

        dataArray[posToIndex(x,y,z)] = id
      }
    }
  }

  dataGenerationOutput.add(Pair(Vector2i(1,2), dataArray))
}


private fun processChunks() {
  if (dataGenerationOutput.isEmpty()) return

  val gotten: Pair<Vector2ic, IntArray>

  try {
    gotten = dataGenerationOutput.remove()!!
  } catch (e: Exception) {
    println("processChunks: failed.")
    return
  }

  val (position, chunkData) = gotten

  data[position] = chunkData

  // Separate internal pointer
  val dataClone = chunkData.clone()

  fullBuildChunkMesh(position.x(), position.y(), dataClone)

  // done
}

//? note: Begin chunk mesh internal api.

@JvmRecord
data class ChunkMesh(val positions: FloatArray, val textureCoords: FloatArray, val indices: IntArray)// todo: this needs to be built into the mesh interface, light: FloatArray)

private fun meshIDExists(pos: Vector2ic): Boolean {
  return meshIDs.containsKey(pos)
}
private fun safetGetMeshIDArray(pos: Vector2ic): Array<String> {
  return meshIDs[pos] ?: throw RuntimeException("world: tried to access nonexistent chunk mesh: ${pos.x()}, ${pos.y()}")
}

private fun receiveChunkMeshes() {
  //todo: This will automatically upload the generated chunks via the mesh interface.
}

private fun fullBuildChunkMesh(posX: Int, posZ: Int, chunkData: IntArray) {
  //? note: This builds out the initial chunk mesh components.
  val (leftExists, left) = safeGetDataDeconstruct(posX - 1, posZ)
  val (rightExists, right) = safeGetDataDeconstruct(posX + 1, posZ)
  val (backExists, back) = safeGetDataDeconstruct(posX, posZ + 1)
  val (frontExists, front) = safeGetDataDeconstruct(posX, posZ - 1)

//  println("buildChunkMesh is running")
}
