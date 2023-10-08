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

private const val Y_SLICE_HEIGHT   = 16
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

//  ArrayList<Float>
  val positions = ArrayList<Float>()
  val textureCoords = ArrayList<Float>()
  val indices = ArrayList<Int>()
  val colors = ArrayList<Float>()


  for (i in 0 until MESH_ARRAY_SIZE) {
    buildMesh(
      i, chunkData, leftExists, left,
      rightExists, right,
      backExists, back,
      frontExists, front,
      positions, textureCoords,
      indices, colors
    )
  }
}

private fun buildMesh(
  posY: Int, chunkData: IntArray, leftExists: Boolean, leftChunk: IntArray,
  rightExists: Boolean, rightChunk: IntArray,
  backExists: Boolean, backChunk: IntArray,
  frontExists: Boolean, frontChunk: IntArray,
  positions: ArrayList<Float>,
  textureCoords: ArrayList<Float>,
  indices: ArrayList<Int>,
  colors: ArrayList<Float>
) {

  for (y in (Y_SLICE_HEIGHT * posY) until (Y_SLICE_HEIGHT * (posY + 1))) {
    for (z in 0 until DEPTH) {
      for (x in 0 until WIDTH) {
        val currentBlock = chunkData[posToIndex(x,y,z)]
        if (currentBlock.getBlockID() == 0) continue

        when (block.getDrawType(currentBlock.getBlockID())) {
          DrawType.AIR -> continue
          DrawType.BLOCK -> {
            val left = detectNeighbor(x,y,z,0,chunkData,leftExists,leftChunk)
            val right = detectNeighbor(x,y,z,1,chunkData,rightExists,rightChunk)
            val front = detectNeighbor(x,y,z,2,chunkData,frontExists,frontChunk)
            val back = detectNeighbor(x,y,z,3,chunkData,backExists,backChunk)
            val bottom = detectNeighbor(x,y,z,4, chunkData,false, IntArray(0))
            val top = detectNeighbor(x,y,z,5, chunkData,false,IntArray(0))
            blockDrawTypeAssembly(x,y,z, currentBlock, left, right, front, back, bottom, top, positions, textureCoords, indices, colors)
          }
          DrawType.BLOCK_BOX -> TODO()
          DrawType.TORCH -> TODO()
          DrawType.LIQUID_SOURCE -> TODO()
          DrawType.LIQUID_FLOW -> TODO()
          DrawType.GLASS -> TODO()
          DrawType.PLANT -> TODO()
          DrawType.LEAVES -> TODO()
        }
      }
    }
  }
}

private fun blockDrawTypeAssembly(
  x: Int, y: Int, z: Int,
  currentBlock: Int,
  left: Int, right: Int,
  front: Int, back: Int,
  bottom: Int, top: Int,
  positions: ArrayList<Float>,
  textureCoords: ArrayList<Float>,
  indices: ArrayList<Int>,
  colors: ArrayList<Float>
) {

  val OVER_PROVISION = 0.00001f;
  fun putPositions(vararg pos: Float) = positions.addAll(pos.asSequence())
  fun putTextureCoords(pos: FloatArray) = textureCoords.addAll(pos.asSequence())
  val iOrder = intArrayOf(0,1,2,2,3,0)
  fun putIndices() {
    val currentSize = (indices.size / 6) * 4
    iOrder.forEach { indices.add(it + currentSize) }
  }
  fun putColors(light: Float) = (0 until 4).forEach { _ -> colors.add(light) }

  // Left.
  when (block.getDrawType(left.getBlockID())) {
    DrawType.BLOCK -> {/*do nothing*/}
    else -> {
      // Attach face.
      putPositions(
        1f + OVER_PROVISION + x, 1f + OVER_PROVISION + y, 0f + z,
        1f + OVER_PROVISION + x, 0f - OVER_PROVISION + y, 0f + z,
        0f - OVER_PROVISION + x, 0f - OVER_PROVISION + y, 0f + z,
        0f - OVER_PROVISION + x, 1f + OVER_PROVISION + y, 0f + z
      )
      putTextureCoords(block.getTextureCoords(currentBlock.getBlockID())[0])
      putIndices()
      putColors(left.getBlockLight().toFloat() / 15f)
    }
  }

  // Right.
  when (block.getDrawType(right.getBlockID())) {
    DrawType.BLOCK -> {/*do nothing*/}
    else -> {
      // Attach face.
      putPositions(
        1f + x, 1f + OVER_PROVISION + y, 1f + OVER_PROVISION + z,
        1f + x, 0f - OVER_PROVISION + y, 1f + OVER_PROVISION + z,
        1f + x, 0f - OVER_PROVISION + y, 0f - OVER_PROVISION + z,
        1f + x, 1f + OVER_PROVISION + y, 0f - OVER_PROVISION + z
      )
      putTextureCoords(block.getTextureCoords(currentBlock.getBlockID())[1])
      putIndices()
      putColors(right.getBlockLight().toFloat() / 15f)
    }
  }

  // Front.
  when (block.getDrawType(front.getBlockID())) {
    DrawType.BLOCK -> {/*do nothing*/}
    else -> {
      // Attach face.
      putPositions(
        1f + OVER_PROVISION + x, 1f + OVER_PROVISION + y, 0f + z,
        1f + OVER_PROVISION + x, 0f - OVER_PROVISION + y, 0f + z,
        0f - OVER_PROVISION + x, 0f - OVER_PROVISION + y, 0f + z,
        0f - OVER_PROVISION + x, 1f + OVER_PROVISION + y, 0f + z
      )
      putTextureCoords(block.getTextureCoords(currentBlock.getBlockID())[2])
      putIndices()
      putColors(front.getBlockLight().toFloat() / 15f)
    }
  }

  // Back.
  when (block.getDrawType(back.getBlockID())) {
    DrawType.BLOCK -> {/*do nothing*/}
    else -> {
      // Attach face.
      putPositions(
        0f - OVER_PROVISION + x, 1f + OVER_PROVISION + y, 1f + z,
        0f - OVER_PROVISION + x, 0f - OVER_PROVISION + y, 1f + z,
        1f + OVER_PROVISION + x, 0f - OVER_PROVISION + y, 1f + z,
        1f + OVER_PROVISION + x, 1f + OVER_PROVISION + y, 1f + z
      )
      putTextureCoords(block.getTextureCoords(currentBlock.getBlockID())[3])
      putIndices()
      putColors(back.getBlockLight().toFloat() / 15f)
    }
  }

  // Bottom.
  when (block.getDrawType(bottom.getBlockID())) {
    DrawType.BLOCK -> {/*do nothing*/}
    else -> {
      // Attach face.
      putPositions(
        1f + OVER_PROVISION + x, 0f + y, 1f + OVER_PROVISION + z,
        0f - OVER_PROVISION + x, 0f + y, 1f + OVER_PROVISION + z,
        0f - OVER_PROVISION + x, 0f + y, 0f - OVER_PROVISION + z,
        1f + OVER_PROVISION + x, 0f + y, 0f - OVER_PROVISION + z
      )
      putTextureCoords(block.getTextureCoords(currentBlock.getBlockID())[4])
      putIndices()
      putColors(bottom.getBlockLight().toFloat() / 15f)
    }
  }

  // Top.
  when (block.getDrawType(top.getBlockID())) {
    DrawType.BLOCK -> {/*do nothing*/}
    else -> {
      // Attach face.
      putPositions(
        1f + OVER_PROVISION + x, 1f + y, 0f - OVER_PROVISION + z,
        0f - OVER_PROVISION + x, 1f + y, 0f - OVER_PROVISION + z,
        0f - OVER_PROVISION + x, 1f + y, 1f + OVER_PROVISION + z,
        1f + OVER_PROVISION + x, 1f + y, 1f + OVER_PROVISION + z
      )
      putTextureCoords(block.getTextureCoords(currentBlock.getBlockID())[4])
      putIndices()
      putColors(top.getBlockLight().toFloat() / 15f)
    }
  }

}

private fun detectNeighbor(
  x: Int, y: Int, z: Int,
  // 0 left 1 right 2 front 3 back 4 bottom 5 top
  dir: Int, chunkData: IntArray,
  neighborExists: Boolean, neighbor: IntArray): Int {

  val containmentCheck = when (dir) {
    0 -> x - 1
    1 -> x + 1
    2 -> z - 1
    3 -> z + 1
    4 -> y - 1
    5 -> y + 1
    else -> throw RuntimeException("detectNeighbor: How 1??")
  }
  val containmentLimit = when (dir) {
    0,1 -> WIDTH
    2,3 -> DEPTH
    4,5 -> HEIGHT
    else -> throw RuntimeException("detectNeighbor: How 2??")
  }

  return if (!(0 until containmentLimit).contains(containmentCheck)) {
    if (neighborExists) {
      val index = when (dir) {
        0 -> posToIndex(x + WIDTH,y,z)
        1 -> posToIndex(x - WIDTH,y,z)
        2 -> posToIndex(x,y,z + DEPTH)
        3 -> posToIndex(x,y,z - DEPTH)
        4,5 -> 0 setBlockLight 15 //! fixme: current natural light when time is implemented!
        else -> throw RuntimeException("detectNeighbor: How 3??")
      }
      neighbor[index]
    } else { 0 }  //! fixme: current natural light when time is implemented!
  } else {
    val index = when (dir) {
      0 -> posToIndex(x - 1,y,z)
      1 -> posToIndex(x + 1,y,z)
      2 -> posToIndex(x,y,z - 1)
      3 -> posToIndex(x,y,z + 1)
      4 -> posToIndex(x,y - 1, z)
      5 -> posToIndex(x,y + 1, z)
      else -> throw RuntimeException("detectNeighbor: How 4??")
    }
    chunkData[index]
  }
}
