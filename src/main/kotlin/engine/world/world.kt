package engine.world


import engine.block.Block
import engine.block.DrawType
import engine.camera.Camera
import engine.client_player.ClientPlayer
import engine.collision.Collision
import engine.joml_bolt_ons.destructure
import engine.model.mesh.Mesh
import engine.noise.Noise
import engine.noise.NoiseType
import engine.thread.Thread
import engine.thread.Thread.parallelForEach
import engine.world.world.addMeshUpdate
import engine.world.world.getBlockID
import engine.world.world.getBlockLight
import engine.world.world.getBlockState
import engine.world.world.idCheck
import engine.world.world.lightCheck
import engine.world.world.setBlockID
import engine.world.world.setBlockLight
import engine.world.world.setBlockState
import engine.world.world.stateCheck
import org.joml.*
import org.joml.Math.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayDeque

/*
This is a data oriented and functional approach to the mess that was in Java.
*/
object world {

  internal const val WIDTH = 16
  internal const val HEIGHT = 128
  internal const val DEPTH = 16
  internal const val X_STRIDE = DEPTH * HEIGHT
  private const val ARRAY_SIZE = WIDTH * HEIGHT * DEPTH
  private const val GRAVITY = 1f

  internal const val Y_SLICE_HEIGHT = 16
  private const val MESH_ARRAY_SIZE = 8

  private var seed = 123_456_789

  private const val MAX_CHUNK_GENS_PER_TICK = 80
  private const val MAX_CHUNK_MESH_PROCS_PER_TICK = 80
  private const val MAX_CHUNK_MESH_UPDATES_PER_TICK = 80
  private const val MAX_CHUNK_PROCS_PER_TICK = 80

  // Fields for the single block API.
  private val chunkPosition = Vector2i(0, 0)
  private val internalPosition = Vector3i(0, 0, 0)
  private val floatingPosition = Vector3f(0f, 0f, 0f)
  private val loadCheck = Vector2i(0, 0)

  // Chunk block data
//! todo: upgrade to LongArray! This will allow either 24 bit or 32 bit limit for chunks!
  private val data = ConcurrentHashMap<Vector2ic, IntArray>()

  private val meshIDs = HashMap<Vector2ic, IntArray>()

  // Input into chunk generator goes into here.
  private val dataGenerationInput = ConcurrentLinkedQueue<Vector2ic>()

  // Output from chunk generator coroutines goes into here.
  private val dataGenerationOutput = ConcurrentLinkedQueue<Pair<Vector2ic, IntArray>>()

  // Input into the chunk mesh generator goes into here.
  private val meshGenerationInput = ConcurrentLinkedQueue<Vector3ic>()

  // Output from the chunk mesh generator goes into here.
  private val meshGenerationOutput = ConcurrentLinkedQueue<Pair<Vector3ic, ChunkMesh>>()

// note: API begins here.

  // note: Single block API begins here.

  fun isLoaded(pos: Vector3fc): Boolean {
    //? note: raw pos wise
    calculateChunkPosition(pos)
    return data.containsKey(chunkPosition)
  }

  internal fun isLoaded(x: Int, z: Int): Boolean {
    //? note: chunk wise
    return data.containsKey(loadCheck.set(x, z))
  }

  fun getBlock(pos: Vector3fc): Int {
    calculatePosition(pos)
    return getSingleBlock()
  }

  fun getBlockID(pos: Vector3fc): Int = getBlock(pos).getBlockID()
  fun getBlockState(pos: Vector3fc): Int = getBlock(pos).getBlockState()
  fun getBlockLight(pos: Vector3fc): Int = getBlock(pos).getBlockLight()

  fun getBlock(x: Float, y: Float, z: Float): Int {
    floatingPosition.set(x, y, z)
    calculatePosition(floatingPosition)
    return getSingleBlock()
  }

  fun getBlockID(x: Float, y: Float, z: Float): Int = getBlock(x, y, z).getBlockID()
  fun getBlockState(x: Float, y: Float, z: Float): Int = getBlock(x, y, z).getBlockState()
  fun getBlockLight(x: Float, y: Float, z: Float): Int = getBlock(x, y, z).getBlockLight()

  fun setBlock(pos: Vector3fc, newBlock: Int) {
    newBlock.getBlockID().idCheck()
    newBlock.getBlockState().stateCheck()
    newBlock.getBlockLight().lightCheck()
    if (!Block.has(newBlock.getBlockID())) throw RuntimeException("world: ${newBlock.getBlockID()} is not a registered block.")
    calculatePosition(pos)
    throwIfNonExistent(chunkPosition)
    data[chunkPosition]!![posToIndex(internalPosition)] = newBlock
    addSingleBlockMeshUpdate()
  }

  fun setBlockID(pos: Vector3fc, newBlockID: Int) = setBlock(pos, getBlock(pos) setBlockID newBlockID)
  fun setBlockState(pos: Vector3fc, newState: Int) = setBlock(pos, getBlock(pos) setBlockState newState)
  fun steBlockLight(pos: Vector3fc, newLight: Int) = setBlock(pos, getBlock(pos) setBlockLight newLight)

  fun setBlock(x: Float, y: Float, z: Float, newBlock: Int) {
    floatingPosition.set(x, y, z)
    setBlock(floatingPosition, newBlock)
  }

  fun setBlockID(x: Float, y: Float, z: Float, newBlockID: Int) =
    setBlock(x, y, z, getBlock(x, y, z) setBlockID newBlockID)

  fun setBlockState(x: Float, y: Float, z: Float, newState: Int) =
    setBlock(x, y, z, getBlock(x, y, z) setBlockState newState)

  fun steBlockLight(x: Float, y: Float, z: Float, newLight: Int) =
    setBlock(x, y, z, getBlock(x, y, z) setBlockLight newLight)

  private fun addSingleBlockMeshUpdate() {
    val x = internalPosition.x()
    val y = internalPosition.y()
    val z = internalPosition.z()

    val chunkX = chunkPosition.x()
    val chunkZ = chunkPosition.y()

    val yStack = toYStack(y)

    if (x == 0) {
      addSingleBlockMeshUpdateIfLoaded(chunkX - 1, yStack, chunkZ)
    } else if (x == 15) {
      addSingleBlockMeshUpdateIfLoaded(chunkX + 1, yStack, chunkZ)
    }

    if (z == 0) {
      addSingleBlockMeshUpdateIfLoaded(chunkX, yStack, chunkZ - 1)
    } else if (z == 15) {
      addSingleBlockMeshUpdateIfLoaded(chunkX, yStack, chunkZ + 1)
    }

    if (y > 0 && (y + 1) % 16 == 0) {
      addSingleBlockMeshUpdateIfLoaded(chunkX, yStack + 1, chunkZ)
    } else if (y < HEIGHT - 1 && y % 16 == 0) {
      addSingleBlockMeshUpdateIfLoaded(chunkX, yStack - 1, chunkZ)
    }
//    println(16 / 16f)

    addMeshUpdate(chunkX, yStack, chunkZ)
  }

  private fun addSingleBlockMeshUpdateIfLoaded(x: Int, y: Int, z: Int) {
    if (isLoaded(x, z)) addMeshUpdate(x, y, z)
  }

  internal fun toYStack(y: Int): Int = floor(y / Y_SLICE_HEIGHT.toFloat()).toInt()

  private fun getSingleBlock(): Int {
    return data[chunkPosition]!![posToIndex(internalPosition)]
  }

  private fun calculatePosition(pos: Vector3fc) {
    calculateChunkPosition(pos)
    throwIfNonExistent(chunkPosition)
    calculateInternalPosition(pos)
  }

  private fun calculateInternalPosition(pos: Vector3fc) =
    internalPosition.set(internalX(floor(pos.x())), floor(pos.y()).toInt(), internalZ(floor(pos.z())))

  internal fun internalX(x: Float): Int =
    if (x < 0) (WIDTH - floor(abs(x + 1) % WIDTH).toInt()) - 1 else floor(x % WIDTH).toInt()

  internal fun internalZ(z: Float): Int =
    if (z < 0) (DEPTH - floor(abs(z + 1) % DEPTH)).toInt() - 1 else floor(z % DEPTH).toInt()

  internal fun internalX(x: Int): Int = internalX(x.toFloat())
  internal fun internalZ(z: Int): Int = internalZ(z.toFloat())

  private fun calculateChunkPosition(pos: Vector3fc) = chunkPosition.set(toChunkX(pos.x()), toChunkZ(pos.z()))
  internal fun toChunkX(x: Float): Int = floor(x / WIDTH).toInt()
  internal fun toChunkZ(z: Float): Int = floor(z / DEPTH).toInt()
  internal fun toChunkX(x: Int): Int = toChunkX(x.toFloat())
  internal fun toChunkZ(z: Int): Int = toChunkZ(z.toFloat())

  private fun throwIfNonExistent(pos: Vector2ic) {
    if (!data.containsKey(pos)) throw RuntimeException("world: Tried to get ${pos.x()},${pos.y()} which doesn't exist.")
  }

  // note: Rest begins here.

  fun getGravity(): Float {
    return GRAVITY * 2f
  }

  fun getChunkWidth(): Int = WIDTH
  fun getChunkWidthFloat(): Float = WIDTH.toFloat()

  fun getChunkHeight(): Int = HEIGHT
  fun getChunkHeightFloat(): Float = HEIGHT.toFloat()

  fun getChunkDepth(): Int = DEPTH
  fun getChunkDepthFloat(): Float = DEPTH.toFloat()

  fun generateChunk(x: Int, y: Int) {
    val key = Vector2i(x, y)
    if (data.containsKey(key) || dataGenerationInput.contains(key)) {
//      println("Discarding generation $x, $y")
      return
    }
    dataGenerationInput.add(key)
  }

  internal fun cleanAndGenerationScan() {
    val clientChunkPosition = ClientPlayer.getChunkPosition()
    val renderDistance = Camera.getRenderDistance()

//    println("update")

    val (currentX, currentZ) = clientChunkPosition.destructure()

    discardOldChunks(currentX, currentZ, renderDistance)

    (0..renderDistance).parallelForEach { rad ->
      ((currentX - rad)..(currentX + rad)).forEach { x ->
        ((currentZ - rad)..(currentZ + rad)).forEach { z ->
          val currentKey = Vector2i(x, z)
          if (!data.containsKey(currentKey)) {
            generateChunk(x, z)
          }
        }
      }
    }
  }


  private val meshDestructionQueue = ArrayDeque<Vector2ic>()

  private fun discardOldChunks(currentX: Int, currentZ: Int, renderDistance: Int) {
    Thread.launch {
      val dataDestructionQueue = ArrayDeque<Vector2ic>()
      data.forEach { (key: Vector2ic, _) ->
        val (x, z) = key.destructure()
        if (abs(x - currentX) > renderDistance || abs(z - currentZ) > renderDistance) {
          dataDestructionQueue.add(key)
        }
      }
      while (dataDestructionQueue.isNotEmpty()) {
        val key = dataDestructionQueue.removeFirst()
        data.remove(key)
      }
    }

    //? note: due to concurrency, we must also check the mesh list.
    meshIDs.forEach { (key: Vector2ic, _) ->
      val (x, z) = key.destructure()
      if (abs(x - currentX) > renderDistance || abs(z - currentZ) > renderDistance) {
        meshDestructionQueue.add(key)
      }
    }

    while (meshDestructionQueue.isNotEmpty()) {
      val key = meshDestructionQueue.removeFirst()
      if (meshIDs.containsKey(key)) {
        meshIDs[key]!!.forEach { id ->
          if (id != 0) {
            Mesh.destroy(id)
          }
        }
      }
      meshIDs.remove(key)
    }
  }

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

  fun Int.idCheck() {
    if (!(0..65535).contains(this)) throw RuntimeException("Passed in value larger than 16 bits to block id.")
  }

  fun Int.getBlockLight(): Int {
    return this shl 16 ushr 28
  }

  fun Int.lightCheck() {
    if (!(0..15).contains(this)) throw RuntimeException("Passed in value larger than 4 bits to block light.")
  }

  fun Int.getBlockState(): Int {
    return this shl 20 ushr 28
  }

  fun Int.stateCheck() {
    if (!(0..15).contains(this)) throw RuntimeException("Passed in value larger than 4 bits to block state.")
  }

  infix fun Int.setBlockID(newID: Int): Int {
    newID.idCheck()
    return combine(newID.shiftBlock(), this.parseBlockLight(), this.parseBlockState())
  }

  infix fun Int.setBlockLight(newLight: Int): Int {
    newLight.lightCheck()
    return combine(this.parseBlockID(), newLight.shiftLight(), this.parseBlockState())
  }

  infix fun Int.setBlockState(newState: Int): Int {
    newState.stateCheck()
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

  fun posToIndex(pos: Vector3ic): Int = posToIndex(pos.x(), pos.y(), pos.z())

  fun posToIndex(x: Int, y: Int, z: Int): Int {
    return (x * X_STRIDE) + (z * HEIGHT) + y
  }

  fun indexToPos(i: Int): Vector3ic {
    return Vector3i(
      i / X_STRIDE,
      i % HEIGHT,
      (i / HEIGHT) % DEPTH
    )
  }

// note: Internal begins here

  internal fun safetGetData(posX: Int, posZ: Int): IntArray {
    return data[Vector2i(posX, posZ)]
      ?: throw RuntimeException("world: tried to access nonexistent chunk: $posX, $posZ")
  }

  private fun safeGetDataDeconstructClone(posX: Int, posZ: Int): Pair<Boolean, IntArray> {
    val gottenData = try {
      data[Vector2i(posX, posZ)]!!.clone()
    } catch (e: Exception) {
      IntArray(0)
    }
    val exists = gottenData.isNotEmpty()
    return Pair(exists, gottenData)
  }


  internal fun disperseChunkGenerators() {
    //note: Wrapper function to make implementation cleaner.
    // Shoot and forget. More like a machine gun.

//    println(Thread.activeCount())
//    if (currentCoroutine != null) {
//      println(currentCoroutine!!.isActive)
//      currentCoroutine = null
//    }

    // If there's nothing to be done, do nothing.
    for (i in 0..MAX_CHUNK_MESH_PROCS_PER_TICK) {
      if (meshGenerationOutput.isEmpty()) break
      receiveChunkMeshes()
    }

    Thread.launch {
      for (i in 0..MAX_CHUNK_MESH_UPDATES_PER_TICK) {
        if (meshGenerationInput.isEmpty()) break
        processMeshUpdate()
      }
    }

    Thread.launch {
      for (i in 0..MAX_CHUNK_GENS_PER_TICK) {
        if (dataGenerationInput.isEmpty()) break
        genChunk()
      }
    }

    Thread.launch {
      for (i in 0..MAX_CHUNK_PROCS_PER_TICK) {
        if (dataGenerationOutput.isEmpty()) break
        processChunks()
      }
    }
  }

  private fun genChunk() {
    //note: Async double check.
    if (dataGenerationInput.isEmpty()) return

    val gotten: Vector2ic

    try {
      gotten = dataGenerationInput.remove()!!
    } catch (e: Exception) {
//      println("genChunk: Data race failure.")
      return
    }

    if (data.containsKey(gotten)) return

    val (xOffset, zOffset) = gotten.destructure()

//  println("Generating: $xOffset, $zOffset")

    val grass = Block.getID("crafter:grass")
    val dirt = Block.getID("crafter:dirt")
    val stone = Block.getID("crafter:stone")

    val biomeFrequency = 0.01f
    val biomeScale = 20f
    val biomeBaseHeight = 60

    val noise = Noise(seed)
    noise.setNoiseType(NoiseType.Simplex)
    noise.setFrequency(biomeFrequency)

    val dataArray = IntArray(ARRAY_SIZE)

    for (x in 0 until WIDTH) {
      for (z in 0 until DEPTH) {

        //note: +0.5 because the output is -0.5 to 0.5
        val calculatedNoise =
          noise.getSimplex((x + (xOffset * WIDTH)).toFloat(), (z + (zOffset * DEPTH)).toFloat()) + 0.5f

        val height = ((calculatedNoise * biomeScale) + biomeBaseHeight).toInt()

        for (y in 0 until HEIGHT) {
          val id = when {
            y < height - 6 -> 0 setBlockID stone setBlockLight 0
            y < height -> 0 setBlockID dirt setBlockLight 0
            y <= height -> 0 setBlockID grass setBlockLight 0
            else -> 0 setBlockID 0 setBlockLight 15
          }
          dataArray[posToIndex(x, y, z)] = id
        }
      }
    }

    dataGenerationOutput.add(Pair(gotten, dataArray))
  }


  private fun processChunks() {

    //? note: This is where the generated chunks are received.
    //? note: Fires off 8 chunk mesh generations. External thread.

    if (dataGenerationOutput.isEmpty()) return

    val gotten: Pair<Vector2ic, IntArray>

    try {
      gotten = dataGenerationOutput.remove()!!
    } catch (e: Exception) {
//      println("processChunks: Data race failure.")
      return
    }

    val (position, chunkData) = gotten

    data[position] = chunkData

    // Fire off current chunk mesh updates.
    fullMeshUpdate(position.x(), position.y())

    // Fire off neighbor chunk mesh updates.
    fullNeighborMeshUpdate(position.x(), position.y())
  }

//? note: Begin chunk mesh internal api.

  @JvmRecord
  private data class ChunkMesh(
    val positions: FloatArray,
    val textureCoords: FloatArray,
    val indices: IntArray,
    val light: FloatArray
  ) {

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as ChunkMesh

      if (!positions.contentEquals(other.positions)) return false
      if (!textureCoords.contentEquals(other.textureCoords)) return false
      if (!indices.contentEquals(other.indices)) return false
      if (!light.contentEquals(other.light)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = positions.contentHashCode()
      result = 31 * result + textureCoords.contentHashCode()
      result = 31 * result + indices.contentHashCode()
      result = 31 * result + light.contentHashCode()
      return result
    }
  }

  private fun fullMeshUpdate(x: Int, z: Int) {
    for (y in 0 until MESH_ARRAY_SIZE) addMeshUpdate(x, y, z)
  }

  private fun fullNeighborMeshUpdate(x: Int, z: Int) {
    for (y in 0 until MESH_ARRAY_SIZE) addMeshUpdate(x + 1, y, z)
    for (y in 0 until MESH_ARRAY_SIZE) addMeshUpdate(x - 1, y, z)
    for (y in 0 until MESH_ARRAY_SIZE) addMeshUpdate(x, y, z + 1)
    for (y in 0 until MESH_ARRAY_SIZE) addMeshUpdate(x, y, z - 1)
  }

  fun renderChunks() {
    val worker3 = Vector3f()
    meshIDs.forEach { (position: Vector2ic, array: IntArray) ->
      array.forEachIndexed inner@{ y, id ->
        if (id == 0) return@inner

        val renderX = (position.x() * WIDTH).toFloat()
        val testY = (y * Y_SLICE_HEIGHT).toFloat()
        val renderZ = (position.y() * DEPTH).toFloat()

        if (!Collision.chunkMeshWithinFrustum(renderX, testY, renderZ)) return@inner

        worker3.set(renderX, 0f, renderZ)
        Camera.setObjectMatrix(worker3)
        Mesh.draw(id)
//      println("drawing: ${position.x()}, ${position.y()}")
      }
    }
  }

  private fun meshIDExists(pos: Vector2ic): Boolean {
    return meshIDs.containsKey(pos)
  }

  private fun safetGetMeshIDArray(pos: Vector2ic): IntArray {
    return meshIDs[pos]
      ?: throw RuntimeException("world: tried to access nonexistent chunk mesh: ${pos.x()}, ${pos.y()}")
  }

  private fun putOrCreatePutMesh(pos: Vector3ic, id: Int) {
    val key = Vector2i(pos.x(), pos.z())
    if (!meshIDs.containsKey(key)) meshIDs[key] = IntArray(MESH_ARRAY_SIZE)
    val currentArray = meshIDs[key]!!
    if (currentArray[pos.y()] != 0) {
      Mesh.destroy(currentArray[pos.y()])
    }
    currentArray[pos.y()] = id
//  println("put ${pos.x()} ${pos.z()} into ${pos.y()}")
  }

  fun addMeshUpdate(x: Int, y: Int, z: Int) {
    val newAdd = Vector3i(x, y, z)
    if (meshGenerationInput.contains(newAdd)) return
    meshGenerationInput.add(newAdd)
  }

  private fun receiveChunkMeshes() {

    //? note: This receives the generated meshes. Uploads them into GPU. Main thread.

    val (position, data) = meshGenerationOutput.remove()

    if (data.positions.isEmpty()) {
      //? note: received a deletion update.
      putOrCreatePutMesh(position, 0)
    } else {
      //? note: received a normal update.
      val uuid = UUID.randomUUID().toString()
      val id = Mesh.create3D(uuid, data.positions, data.textureCoords, data.indices, data.light, "worldAtlas")
      putOrCreatePutMesh(position, id)
    }
  }

  private fun processMeshUpdate() {

    //? note: Process one element in the input queue. External thread.

    if (meshGenerationInput.isEmpty()) return

    val pos: Vector3ic

    try {
      pos = meshGenerationInput.remove()!!
    } catch (e: Exception) {
//      println("processMeshUpdate: Data race failure.")
      return
    }

    val posX = pos.x()
    val posY = pos.y()
    val posZ = pos.z()

//  pos.print("gotten")

    val dataClone: IntArray = try {
      data[Vector2i(posX, posZ)]!!.clone()
    } catch (e: Exception) {
//    println("processMeshUpdate: $posX, $posZ does not exist.")
      return
    }

    val (leftExists, left) = safeGetDataDeconstructClone(posX - 1, posZ)
    val (rightExists, right) = safeGetDataDeconstructClone(posX + 1, posZ)
    val (frontExists, front) = safeGetDataDeconstructClone(posX, posZ - 1)
    val (backExists, back) = safeGetDataDeconstructClone(posX, posZ + 1)

    val positions = ArrayList<Float>()
    val textureCoords = ArrayList<Float>()
    val indices = ArrayList<Int>()
    val colors = ArrayList<Float>()

    buildMesh(
      pos.y(), dataClone, leftExists, left,
      rightExists, right,
      backExists, back,
      frontExists, front,
      positions, textureCoords,
      indices, colors
    )

    //? note: We have to send blank meshes to the output so they can be deleted in gpu memory.

    meshGenerationOutput.add(
      Pair(
        Vector3i(posX, posY, posZ),
        ChunkMesh(positions.toFloatArray(), textureCoords.toFloatArray(), indices.toIntArray(), colors.toFloatArray())
      )
    )
  }

  private fun buildMesh(
    posY: Int, chunkData: IntArray,
    leftExists: Boolean, leftChunk: IntArray,
    rightExists: Boolean, rightChunk: IntArray,
    backExists: Boolean, backChunk: IntArray,
    frontExists: Boolean, frontChunk: IntArray,
    positions: ArrayList<Float>,
    textureCoords: ArrayList<Float>,
    indices: ArrayList<Int>,
    colors: ArrayList<Float>
  ) {

    for (z in 0 until DEPTH) {
      for (x in 0 until WIDTH) {
        for (y in (Y_SLICE_HEIGHT * posY) until (Y_SLICE_HEIGHT * (posY + 1))) {
          val currentBlock = chunkData[posToIndex(x, y, z)]
          if (currentBlock.getBlockID() == 0) continue

          when (Block.getDrawType(currentBlock.getBlockID())) {
            DrawType.AIR -> continue
            DrawType.BLOCK -> {
              val left = detectNeighbor(x, y, z, 0, chunkData, leftExists, leftChunk)
              val right = detectNeighbor(x, y, z, 1, chunkData, rightExists, rightChunk)
              val front = detectNeighbor(x, y, z, 2, chunkData, frontExists, frontChunk)
              val back = detectNeighbor(x, y, z, 3, chunkData, backExists, backChunk)
              val bottom = detectNeighbor(x, y, z, 4, chunkData, false, IntArray(0))
              val top = detectNeighbor(x, y, z, 5, chunkData, false, IntArray(0))
              blockDrawTypeAssembly(
                x,
                y,
                z,
                currentBlock,
                left,
                right,
                front,
                back,
                bottom,
                top,
                positions,
                textureCoords,
                indices,
                colors
              )
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

    val overProvision = 0.00001f
    fun putPositions(vararg pos: Float) = positions.addAll(pos.asSequence())
    fun putTextureCoords(pos: FloatArray) = textureCoords.addAll(pos.asSequence())
    val iOrder = intArrayOf(0, 1, 2, 2, 3, 0)
    fun putIndices() {
      val currentSize = (indices.size / 6) * 4
      iOrder.forEach { indices.add(it + currentSize) }
    }

    fun putColors(light: Float) = (0 until 4).forEach { _ -> colors.add(light) }

    // Left.
    if (left != -1) {
      when (Block.getDrawType(left.getBlockID())) {
        DrawType.BLOCK -> {/*do nothing*/
        }

        else -> {
          // Attach face.
          putPositions(
            0f + x, 1f + overProvision + y, 0f - overProvision + z,
            0f + x, 0f - overProvision + y, 0f - overProvision + z,
            0f + x, 0f - overProvision + y, 1f + overProvision + z,
            0f + x, 1f + overProvision + y, 1f + overProvision + z
          )
          putTextureCoords(Block.getTextureCoords(currentBlock.getBlockID())[0])
          putIndices()
          putColors(left.getBlockLight().toFloat() / 15f)
        }
      }
    }

    // Right.
    if (right != -1) {
      when (Block.getDrawType(right.getBlockID())) {
        DrawType.BLOCK -> {/*do nothing*/
        }

        else -> {
          // Attach face.
          putPositions(
            1f + x, 1f + overProvision + y, 1f + overProvision + z,
            1f + x, 0f - overProvision + y, 1f + overProvision + z,
            1f + x, 0f - overProvision + y, 0f - overProvision + z,
            1f + x, 1f + overProvision + y, 0f - overProvision + z
          )
          putTextureCoords(Block.getTextureCoords(currentBlock.getBlockID())[1])
          putIndices()
          putColors(right.getBlockLight().toFloat() / 15f)
        }
      }
    }

    // Front.
    if (front != -1) {
      when (Block.getDrawType(front.getBlockID())) {
        DrawType.BLOCK -> {/*do nothing*/
        }

        else -> {
          // Attach face.
          putPositions(
            1f + overProvision + x, 1f + overProvision + y, 0f + z,
            1f + overProvision + x, 0f - overProvision + y, 0f + z,
            0f - overProvision + x, 0f - overProvision + y, 0f + z,
            0f - overProvision + x, 1f + overProvision + y, 0f + z
          )
          putTextureCoords(Block.getTextureCoords(currentBlock.getBlockID())[2])
          putIndices()
          putColors(front.getBlockLight().toFloat() / 15f)
        }
      }
    }

    // Back.
    if (back != -1) {
      when (Block.getDrawType(back.getBlockID())) {
        DrawType.BLOCK -> {/*do nothing*/
        }

        else -> {
          // Attach face.
          putPositions(
            0f - overProvision + x, 1f + overProvision + y, 1f + z,
            0f - overProvision + x, 0f - overProvision + y, 1f + z,
            1f + overProvision + x, 0f - overProvision + y, 1f + z,
            1f + overProvision + x, 1f + overProvision + y, 1f + z
          )
          putTextureCoords(Block.getTextureCoords(currentBlock.getBlockID())[3])
          putIndices()
          putColors(back.getBlockLight().toFloat() / 15f)
        }
      }
    }

    //? note: Bottom and top have special properties.
    //? note: You can fall out of the world, and you can also build to the top.
    //? note: We must have a special scenario from this. We require mutable data.

    //! fixme: this needs to get the ambient light level!
    val topAdjusted = if (top == -1) 0 setBlockLight 15 else top
    val bottomAdjusted = if (bottom == -1) 0 setBlockLight 15 else bottom

    // Bottom.
    when (Block.getDrawType(bottomAdjusted.getBlockID())) {
      DrawType.BLOCK -> {/*do nothing*/
      }

      else -> {
        // Attach face.
        putPositions(
          1f + overProvision + x, 0f + y, 1f + overProvision + z,
          0f - overProvision + x, 0f + y, 1f + overProvision + z,
          0f - overProvision + x, 0f + y, 0f - overProvision + z,
          1f + overProvision + x, 0f + y, 0f - overProvision + z
        )
        putTextureCoords(Block.getTextureCoords(currentBlock.getBlockID())[4])
        putIndices()
        putColors(bottomAdjusted.getBlockLight().toFloat() / 15f)
      }
    }

    // Top.
    when (Block.getDrawType(topAdjusted.getBlockID())) {
      DrawType.BLOCK -> {/*do nothing*/
      }

      else -> {
        // Attach face.
        putPositions(
          1f + overProvision + x, 1f + y, 0f - overProvision + z,
          0f - overProvision + x, 1f + y, 0f - overProvision + z,
          0f - overProvision + x, 1f + y, 1f + overProvision + z,
          1f + overProvision + x, 1f + y, 1f + overProvision + z
        )
        putTextureCoords(Block.getTextureCoords(currentBlock.getBlockID())[5])
        putIndices()
        putColors(topAdjusted.getBlockLight().toFloat() / 15f)
      }
    }
  }

  private fun detectNeighbor(
    x: Int, y: Int, z: Int,
    // 0 left 1 right 2 front 3 back 4 bottom 5 top
    dir: Int, chunkData: IntArray,
    neighborExists: Boolean, neighbor: IntArray
  ): Int {

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
      0, 1 -> WIDTH
      2, 3 -> DEPTH
      4, 5 -> HEIGHT
      else -> throw RuntimeException("detectNeighbor: How 2??")
    }

    return if (!(0 until containmentLimit).contains(containmentCheck)) {
      if (neighborExists) {
        val index = when (dir) {
          0 -> posToIndex(WIDTH - 1, y, z)
          1 -> posToIndex(0, y, z)
          2 -> posToIndex(x, y, DEPTH - 1)
          3 -> posToIndex(x, y, 0)
          4, 5 -> 0 setBlockLight 15 //! fixme: current natural light when time is implemented!
          else -> throw RuntimeException("detectNeighbor: How 3??")
        }
        neighbor[index]
      } else {
        -1
      }  //! fixme: current natural light when time is implemented!
    } else {
      val index = when (dir) {
        0 -> posToIndex(x - 1, y, z)
        1 -> posToIndex(x + 1, y, z)
        2 -> posToIndex(x, y, z - 1)
        3 -> posToIndex(x, y, z + 1)
        4 -> posToIndex(x, y - 1, z)
        5 -> posToIndex(x, y + 1, z)
        else -> throw RuntimeException("detectNeighbor: How 4??")
      }
      chunkData[index]
    }
  }
}


object blockManipulator : Iterator<Int> {

  private val LIMIT = Vector3i(64, 128, 64) as Vector3ic
  private val min = Vector3i(0, 0, 0)
  private val max = Vector3i(0, 0, 0)
  private val size = Vector3i(0, 0, 0)
  private var xStride = 0
  private val data = IntArray(LIMIT.x() * LIMIT.y() * LIMIT.z())
  private val minCache = Vector3i(0, 0, 0)
  private val maxCache = Vector3i(0, 0, 0)
  private var skipSingleBlockWarning = false
  private val cachePos = Vector3i()
  private var arraySize = 0
  private var currentCount = 0
  private const val WORLD_WIDTH = world.WIDTH
  private const val WORLD_HEIGHT = world.HEIGHT
  private const val WORLD_DEPTH = world.DEPTH
  private const val WORLD_Y_SLICE_HEIGHT = world.Y_SLICE_HEIGHT
  private const val WORLD_X_STRIDE = world.X_STRIDE

//  internal fun getSize(): Vector3ic {
//    return size
//  }

  fun set(newMin: Vector3fc, newMax: Vector3fc) = set(
    floor(newMin.x()).toInt(),
    floor(newMin.y()).toInt(),
    floor(newMin.z()).toInt(),
    floor(newMax.x()).toInt(),
    floor(newMax.y()).toInt(),
    floor(newMax.z()).toInt()
  )

  fun set(xMin: Float, yMin: Float, zMin: Float, xMax: Float, yMax: Float, zMax: Float): Boolean = set(
    minCache.set(floor(xMin).toInt(), floor(yMin).toInt(), floor(zMin).toInt()),
    maxCache.set(floor(xMax).toInt(), floor(yMax).toInt(), floor(zMax).toInt())
  )

  fun set(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int): Boolean =
    set(minCache.set(xMin, yMin, zMin), maxCache.set(xMax, yMax, zMax))

  fun set(newMin: Vector3ic, newMax: Vector3ic): Boolean {

    // Returns true if all chunks are loaded, for now

//    println("minZ: ${newMin.z()}")

    min.set(newMin)
    max.set(newMax)

    if (!skipSingleBlockWarning) {
      // todo: remove this nonsense
      checkIfSingle()
    }

    checkMinMaxValidity()
    checkSizeValidity()
    checkYAxis()

//    val forceLoad = false
//
//    when {
//      // fixme: This is a "gentle stroll" into automating force loads.
//      forceLoad -> forceLoad()
//      else -> checkArea()
//    }

    size.set((abs(max.x() - min.x()) + 1), (abs(max.y() - min.y()) + 1), (abs(max.z() - min.z()) + 1))

//    yStride = if (size.x == 1 || size.z == 1) {
//      size.x() + size.z()
//    } else {
//      size.x() * size.z()
//    }

    xStride = size.z * size.y

    arraySize = size.x() * size.y() * size.z()

    skipSingleBlockWarning = false

    return read()
  }

  private fun read(): Boolean {
    var allLoaded = true
    val minChunkX = toChunkX(min.x())
    val maxChunkX = toChunkX(max.x())
    val minChunkZ = toChunkZ(min.z())
    val maxChunkZ = toChunkZ(max.z())

    for (chunkX in minChunkX..maxChunkX) {
      for (chunkZ in minChunkZ..maxChunkZ) {

        if (!world.isLoaded(chunkX, chunkZ)) {
          allLoaded = false
          continue
        }

        val gottenData = world.safetGetData(chunkX, chunkZ)

        // Iterating over in world positions.
        for (x in min.x()..max.x()) {
          if (chunkX != toChunkX(x)) continue
          for (z in min.z()..max.z()) {
            if (chunkZ != toChunkZ(z)) continue
            for (y in min.y()..max.y()) {
              data[posToIndex(x, y, z)] = gottenData[worldPosToIndex(internalX(x), y, internalZ(z))]
            }
          }
        }
      }
    }
    return allLoaded
  }

  fun setRaw(index: Int, blockData: Int) {
    indexCheck(index)
    blockData.getBlockID().idCheck()
    blockData.getBlockState().stateCheck()
    blockData.getBlockLight().lightCheck()
    data[index] = blockData
  }

  fun setRaw(x: Int, y: Int, z: Int, blockData: Int) {
    posCheck(x, y, z)
    blockData.getBlockID().idCheck()
    blockData.getBlockState().stateCheck()
    blockData.getBlockLight().lightCheck()
    val index = posToIndex(x, y, z)
    data[index] = blockData
  }

  fun setRaw(pos: Vector3ic, blockData: Int) = setRaw(pos.x(), pos.y(), pos.z(), blockData)

  fun setID(index: Int, id: Int) {
    indexCheck(index)
    id.idCheck()
    data[index] = data[index] setBlockID id
  }

  fun setID(x: Int, y: Int, z: Int, id: Int) {
    posCheck(x, y, z)
    id.idCheck()
    val index = posToIndex(x, y, z)
    data[index] = data[index] setBlockID id
  }

  fun setID(pos: Vector3ic, id: Int) = setID(pos.x(), pos.y(), pos.z(), id)

  fun setState(index: Int, state: Int) {
    indexCheck(index)
    state.stateCheck()
    data[index] = data[index] setBlockState state
  }

  fun setState(x: Int, y: Int, z: Int, state: Int) {
    posCheck(x, y, z)
    state.stateCheck()
    val index = posToIndex(x, y, z)
    data[index] = data[index] setBlockState state
  }

  fun setState(pos: Vector3ic, state: Int) = setState(pos.x(), pos.y(), pos.z(), state)

  fun setLight(index: Int, light: Int) {
    indexCheck(index)
    light.lightCheck()
    data[index] = data[index] setBlockLight light
  }

  fun setLight(x: Int, y: Int, z: Int, light: Int) {
    posCheck(x, y, z)
    light.lightCheck()
    val index = posToIndex(x, y, z)
    data[index] = data[index] setBlockLight light
  }

  fun setLight(pos: Vector3ic, light: Int) = setLight(pos.x(), pos.y(), pos.z(), light)

  fun get(index: Int): Int {
    indexCheck(index)
    return data[index]
  }

  fun get(x: Int, y: Int, z: Int): Int {
    posCheck(x, y, z)
    return data[posToIndex(x, y, z)]
  }

  fun get(pos: Vector3ic): Int {
    posCheck(pos.x(), pos.y(), pos.z())
    return data[posToIndex(pos.x(), pos.y(), pos.z())]
  }

  fun getID(index: Int): Int {
    indexCheck(index)
    return data[index].getBlockID()
  }

  fun getID(x: Int, y: Int, z: Int): Int {
    posCheck(x, y, z)
    return data[posToIndex(x, y, z)].getBlockID()
  }

  fun getID(pos: Vector3ic): Int {
    posCheck(pos.x(), pos.y(), pos.z())
    return data[posToIndex(pos.x(), pos.y(), pos.z())].getBlockID()
  }

  fun getState(index: Int): Int {
    indexCheck(index)
    return data[index].getBlockState()
  }

  fun getState(x: Int, y: Int, z: Int): Int {
    posCheck(x, y, z)
    return data[posToIndex(x, y, z)].getBlockState()
  }

  fun getState(pos: Vector3ic): Int {
    posCheck(pos.x(), pos.y(), pos.z())
    return data[posToIndex(pos.x(), pos.y(), pos.z())].getBlockState()
  }

  fun getLight(index: Int): Int {
    indexCheck(index)
    return data[index].getBlockLight()
  }

  fun getLight(x: Int, y: Int, z: Int): Int {
    posCheck(x, y, z)
    return data[posToIndex(x, y, z)].getBlockLight()
  }

  fun getLight(pos: Vector3ic): Int {
    posCheck(pos.x(), pos.y(), pos.z())
    return data[posToIndex(pos.x(), pos.y(), pos.z())].getBlockLight()
  }


  fun getMin(): Vector3ic {
    return min
  }

  fun getMax(): Vector3ic {
    return max
  }

  private fun indexCheck(index: Int) {
    if (index >= arraySize) throw RuntimeException("blockManipulator: Indexing out of bounds. $arraySize limit, tried $index")
  }

  private fun posCheck(x: Int, y: Int, z: Int) {
    fun thrower(axis: String, min: Int, max: Int, gotten: Int) {
      throw RuntimeException("blockManipulator: $gotten is out of bounds for axis $axis. Min: $min | Max: $max")
    }
    when {
      !(min.x()..max.x()).contains(x) -> thrower("x", min.x(), max.x(), x)
      !(min.y()..max.y()).contains(y) -> thrower("y", min.y(), max.y(), y)
      !(min.z()..max.z()).contains(z) -> thrower("z", min.z(), max.z(), z)
    }
  }

  fun inBounds(x: Int, y: Int, z: Int): Boolean {
    return (min.x()..max.x()).contains(x) && (min.y()..max.y()).contains(y) && (min.z()..max.z()).contains(z)
  }

  fun write() {
    val minChunkX = toChunkX(min.x())
    val maxChunkX = toChunkX(max.x())
    val minChunkZ = toChunkZ(min.z())
    val maxChunkZ = toChunkZ(max.z())

    // These hold the "cube" that makes up the min and max of the area changed. In chunk positions.
    var minXChange = Int.MAX_VALUE
    var maxXChange = Int.MIN_VALUE

    var minYChange = Int.MAX_VALUE
    var maxYChange = Int.MIN_VALUE

    var minZChange = Int.MAX_VALUE
    var maxZChange = Int.MIN_VALUE

    fun updateMinMax(x: Int, y: Int, z: Int) {
      if (x < minXChange) minXChange = x
      if (x > maxXChange) maxXChange = x

      if (y < minYChange) minYChange = y
      if (y > maxYChange) maxYChange = y

      if (z < minZChange) minZChange = z
      if (z > maxZChange) maxZChange = z
    }

    fun finalize() {
      minXChange = toChunkX(minXChange - 1)
      maxXChange = toChunkX(maxXChange + 1)

      minYChange = toYStack(clamp(0, 127, minYChange - 1))
      maxYChange = toYStack(clamp(0, 127, maxYChange + 1))

      minZChange = toChunkZ(minZChange - 1)
      maxZChange = toChunkZ(maxZChange + 1)
    }


    for (chunkX in minChunkX..maxChunkX) {
      for (chunkZ in minChunkZ..maxChunkZ) {
        if (!world.isLoaded(chunkX, chunkZ)) continue
        val gottenData = world.safetGetData(chunkX, chunkZ)
        // Iterating over in world positions.
        for (x in min.x()..max.x()) {
          if (chunkX != toChunkX(x)) continue
          for (z in min.z()..max.z()) {
            if (chunkZ != toChunkZ(z)) continue
            for (y in min.y()..max.y()) {
              val worldIndex = worldPosToIndex(internalX(x), y, internalZ(z))
              val localIndex = posToIndex(x, y, z)
              val oldData = gottenData[worldIndex]
              val newData = data[localIndex]
              if (newData != oldData) {
                gottenData[worldIndex] = newData
                updateMinMax(x, y, z)
              }
            }
          }
        }
      }
    }

    finalize()
    for (x in minXChange..maxXChange) {
      for (z in minZChange..maxZChange) {
        for (y in minYChange..maxYChange) {
          addMeshUpdate(x, y, z)
        }
      }
    }
  }

  private fun worldPosToIndex(x: Int, y: Int, z: Int): Int {
    return (x * WORLD_X_STRIDE) + (z * WORLD_HEIGHT) + y
  }

  fun posToIndex(posX: Int, posY: Int, posZ: Int): Int {
    // This x,y,z portion transforms the real position into a base 0 position.
    val x = posX - min.x
    val y = posY - min.y
    val z = posZ - min.z
    when {
      x < 0 -> println("x was $posX")
      y < 0 -> println("y was $posY")
      z < 0 -> println("z was $posZ")
    }

    return (x * xStride) + (z * size.y) + y
  }

  /*
  return Vector3i(
    i / X_STRIDE,
    i % HEIGHT,
    (i / HEIGHT) % DEPTH
  )
   */
  fun indexToPos(i: Int): Vector3ic {
    return cachePos.set(
      (i / xStride) + min.x,
      (i % size.y) + min.y,
      ((i / size.y) % size.z) + min.z
    )
  }

//  private fun checkArea() {
//    val minChunkX = world.toChunkX(min.x())
//    val maxChunkX = world.toChunkX(max.x())
//    val minChunkZ = world.toChunkZ(min.z())
//    val maxChunkZ = world.toChunkZ(max.z())
//    for (x in minChunkX .. maxChunkX) {
//      for (z in minChunkZ..maxChunkZ) {
//        if (!world.isLoaded(x, z)) {
//          throw RuntimeException("blockManip")
//        }
//      }
//    }
//  }

  private fun forceLoad() {
    val minChunkX = toChunkX(min.x())
    val maxChunkX = toChunkX(max.x())
    val minChunkZ = toChunkZ(min.z())
    val maxChunkZ = toChunkZ(max.z())
    for (x in minChunkX..maxChunkX) {
      for (z in minChunkZ..maxChunkZ) {
        if (!world.isLoaded(x, z)) {
          //fixme: this function doesn't exist yet.
//          world.forceLoadInstant(x,z)
        }
      }
    }
  }

  private fun checkYAxis() {
    min.y = clamp(0, WORLD_HEIGHT - 1, min.y)
    max.y = clamp(0, WORLD_HEIGHT - 1, max.y)
  }

  private fun checkSizeValidity() {
    fun thrower(axis: String) {
      val limiter = when (axis) {
        "x" -> LIMIT.x()
        "y" -> LIMIT.y()
        else -> LIMIT.z()
      }
      throw RuntimeException("blockManipulator: $axis exceeds limit $limiter.")
    }
    when {
      abs(max.x() - min.x()) >= LIMIT.x() -> thrower("x")
      abs(max.y() - min.y()) >= LIMIT.y() -> thrower("y")
      abs(max.z() - min.z()) >= LIMIT.z() -> thrower("z")
    }
  }

  private fun checkMinMaxValidity() {
    fun thrower(axis: String) {
      throw RuntimeException("blockManipulator: min.$axis is greater than max.$axis.")
    }
    when {
      min.x() > max.x() -> thrower("x")
      min.y() > max.y() -> thrower("y")
      min.z() > max.z() -> thrower("z")
    }
  }

  private fun checkIfSingle() {
    if ((abs(max.x() - min.x()) + 1) * (abs(max.y() - min.y()) + 1) * (abs(max.z() - min.z()) + 1) <= 1) {
      println("blockManipulator: Use single block API for single blocks.")
    }
  }

  override fun hasNext(): Boolean {
    val next = currentCount < arraySize
    if (!next) currentCount = 0
    return next
  }

  override fun next(): Int {
    val returning = data[currentCount]
//    println(currentCount)
    currentCount++
    return returning
  }

  fun forEachIndexed(action: (index: Int, Int) -> Unit) {
    var index = 0
    for (item in this) action(index++, item)
  }

  // These are duplicate functions to optimize performance inside this object.
  private fun internalX(x: Float): Int =
    if (x < 0) (WORLD_WIDTH - floor(abs(x + 1) % WORLD_WIDTH).toInt()) - 1 else floor(x % WORLD_WIDTH).toInt()

  private fun internalZ(z: Float): Int =
    if (z < 0) (WORLD_DEPTH - floor(abs(z + 1) % WORLD_DEPTH)).toInt() - 1 else floor(z % WORLD_DEPTH).toInt()

  private fun internalX(x: Int): Int = internalX(x.toFloat())
  private fun internalZ(z: Int): Int = internalZ(z.toFloat())

  private fun toChunkX(x: Float): Int = floor(x / WORLD_WIDTH).toInt()
  private fun toChunkZ(z: Float): Int = floor(z / WORLD_DEPTH).toInt()
  private fun toChunkX(x: Int): Int = toChunkX(x.toFloat())
  private fun toChunkZ(z: Int): Int = toChunkZ(z.toFloat())

  private fun toYStack(y: Int): Int = floor(y / WORLD_Y_SLICE_HEIGHT.toFloat()).toInt()
}

// A nostalgic test
//note: on a 3x3x3 BM test 0 indexed, 26 is the max.
// "you hit my battleship" or, in this case, the end of the array.
// if (posToIndex(x,y,z) == 26) {
//   println("HIT")
// } else if (posToIndex(x,y,z) == 27) throw RuntimeException("OOPS")
