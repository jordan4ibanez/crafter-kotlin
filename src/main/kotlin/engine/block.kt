package engine

import com.fasterxml.jackson.annotation.JsonTypeId
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import java.util.concurrent.ConcurrentHashMap

/*
A concurrent component system to handle blocks.
Basic functional interface.
Indexed by: ID OR translate through name -> ID
*/

//note: con stands for container.
private fun<T> con(): ConcurrentHashMap<Int, T> {
  return ConcurrentHashMap<Int, T>()
}

// Required components.
private val id            = ConcurrentHashMap<String, Int>()
private val name          = con<String>()
private val inventoryName = con<String>()
private val textures      = con<Array<String>>()
private val textureCoords = con<HashMap<String, FloatArray>>()
private val drawType      = con<DrawType>()

// Optional components.
private val walkable           = con<Boolean>()
private val liquid             = con<Boolean>()
private val flow               = con<Int>()
private val viscosity          = con<Int>()
private val climbable          = con<Boolean>()
private val sneakJumpClimbable = con<Boolean>()
private val falling            = con<Boolean>()
private val clear              = con<Boolean>()
private val damagePerSecond    = con<Int>()
private val light              = con<Int>()

enum class DrawType(val data: Int) {
  AIR(0),
  BLOCK(1),
  BLOCK_BOX(2),
  TORCH(3),
  LIQUID_SOURCE(4),
  LIQUID_FLOW(5),
  GLASS(6),
  PLANT(7),
  LEAVES(8);

  fun value(): Int {
    return data
  }
}

fun Int.toDrawType(): DrawType {
  return DrawType.entries.filter { it.data == this }.ifEmpty { throw RuntimeException("$this is not in range of drawtypes (0..8)") }[0]
}

// using this as a namespace.

object block {

// note: setter api begins here.

  fun newBlock(
    name: String,
    inventoryName: String,
    textures: Array<String>,
    drawType: DrawType = DrawType.BLOCK
  ) {
    val id = blockIDCache.assign(name)
    setBlockID(name, id)
    setBlockInventoryName(id, inventoryName)
    setTextures(id, textures)
    setDrawType(id, drawType)
    setInternalName(id, name)
    //todo: Cannot put texture coords here. Need to be generated first
    println("created block $name at $id with drawtype $drawType and inv name $inventoryName")
  }

  // Translators
  private fun setInternalName(id: Int, internalName: String) {
    name[id] = internalName
  }
  fun setBlockID(name: String, newId: Int) {
    id[name] = newId
  }

  // ID oriented.
  fun setBlockInventoryName(id: Int, newName: String) {
    inventoryName[id] = newName
  }
  fun setTextures(id: Int, newTextures: Array<String>) {
    if (newTextures.size != 6) throw RuntimeException("Tried to set block $name textures with ${newTextures.size} size.")
    textures[id] = newTextures
  }
  private fun setTextureCoords(id: Int, newCoords: HashMap<String, FloatArray>) {
    textureCoords[id] = newCoords
  }
  fun setDrawType(id: Int, newDrawType: DrawType) {
    drawType[id] = newDrawType
  }
  fun setWalkable(id: Int, isWalkable: Boolean) {
    walkable[id] = isWalkable
  }
  fun setLiquid(id: Int, isLiquid: Boolean) {
    liquid[id] = isLiquid
  }
  fun setFlow(id: Int, flowLevel: Int) {
    if (!(0..15).contains(flowLevel)) throw RuntimeException("Tried to set block $name with flow level $flowLevel (0..15)")
    flow[id] = flowLevel
  }
  fun setViscosity(id: Int, newViscosity: Int) {
    if (!(0..15).contains(newViscosity)) throw RuntimeException("Tried to set block $name with viscosity $newViscosity (0..15)")
    viscosity[id] = newViscosity
  }
  fun setClimbable(id: Int, isClimbable: Boolean) {
    climbable[id] = isClimbable
  }
  fun setSneakJumpClimbable(id: Int, isSneakJumpClimbable: Boolean) {
    sneakJumpClimbable[id] = isSneakJumpClimbable
  }
  fun setFalling(id: Int, isFalling: Boolean) {
    falling[id] = isFalling
  }
  fun setClear(id: Int, isClear: Boolean) {
    clear[id] = isClear
  }
  fun setDamagePerSecond(id: Int, dps: Int) {
    damagePerSecond[id] = dps
  }
  fun setLight(id: Int, newLight: Int) {
    if (!(0..15).contains(newLight)) throw RuntimeException("Tried to set block $name with viscosity $newLight (0..15)")
    light[id] = newLight
  }

  // Name oriented
  fun setBlockInventoryName(name: String, newName: String) = setBlockInventoryName(getID(name), newName)
  fun setTextures(name: String, newTextures: Array<String>) = setTextures(getID(name), newTextures)
  private fun setTextureCoords(name: String, newCoords: HashMap<String, FloatArray>) = setTextureCoords(getID(name), newCoords)
  fun setDrawType(name: String, newDrawType: DrawType) = setDrawType(getID(name), newDrawType)
  fun setWalkable(name: String, isWalkable: Boolean) = setWalkable(getID(name), isWalkable)
  fun setLiquid(name: String, isLiquid: Boolean) = setLiquid(getID(name), isLiquid)
  fun setFlow(name: String, flowLevel: Int) = setFlow(getID(name), flowLevel)
  fun setViscosity(name: String, newViscosity: Int) = setViscosity(getID(name), newViscosity)
  fun setClimbable(name: String, isClimbable: Boolean)  = setClimbable(getID(name), isClimbable)
  fun setSneakJumpClimbable(name: String, isSneakJumpClimbable: Boolean) = setSneakJumpClimbable(getID(name), isSneakJumpClimbable)
  fun setFalling(name: String, isFalling: Boolean) = setFalling(getID(name), isFalling)
  fun setClear(name: String, isClear: Boolean) = setClear(getID(name), isClear)
  fun setDamagePerSecond(name: String, dps: Int) = setDamagePerSecond(getID(name), dps)
  fun setLight(name: String, newLight: Int) = setLight(getID(name), newLight)

// note: getter api starts here.

  //note: Required - throw errors

  // Translators
  fun getID(name: String): Int {
    return id[name] ?: throw invalidThrow(name, "id")
  }
  fun getName(id: Int): String {
    return name[id] ?: throw invalidThrow(id, "name")
  }

  // ID oriented
  fun getInventoryName(id: Int): String {
    return inventoryName[id] ?: throw invalidThrow(id, "id")
  }
  fun getTextures(id: Int): Array<String> {
    return textures[id] ?: throw invalidThrow(id, "textures")
  }
  fun getDrawType(id: Int): DrawType {
    return drawType[id] ?: throw invalidThrow(id, "drawType")
  }

  // Name oriented
  fun getInventoryName(name: String): String = getInventoryName(getID(name))
  fun getTextures(name: String): Array<String> = getTextures(getID(name))
  fun getDrawType(name: String): DrawType = getDrawType(getID(name))

  //note: Optionals - defaults are set and returned here.

  // ID oriented.
  fun isWalkable(id: Int): Boolean {
    return walkable[id] ?: true
  }
  fun isLiquid(id: Int): Boolean {
    return liquid[id] ?: false
  }
  fun getFlow(id: Int): Int {
    return flow[id] ?: 0
  }
  fun getViscosity(id: Int): Int {
    return viscosity[id] ?: 0
  }
  fun isClimbable(id: Int): Boolean {
    return climbable[id] ?: false
  }
  fun isSneakJumpClimbable(id: Int): Boolean {
    return sneakJumpClimbable[id] ?: false
  }
  fun isFalling(id: Int): Boolean {
    return falling[id] ?: false
  }
  fun isClear(id: Int): Boolean {
    return clear[id] ?: false
  }
  fun getDamagePerSecond(id: Int): Int {
    return damagePerSecond[id] ?: 0
  }
  fun getLight(id: Int): Int {
    return light[id] ?: 0
  }

  // Name oriented.
  fun isWalkable(name: String): Boolean = isWalkable(getID(name))
  fun isLiquid(name: String): Boolean = isLiquid(getID(name))
  fun getFlow(name: String): Int = getFlow(getID(name))
  fun getViscosity(name: String): Int = getViscosity(getID(name))
  fun isClimbable(name: String): Boolean = isClimbable(getID(name))
  fun isSneakJumpClimbable(name: String): Boolean = isSneakJumpClimbable(getID(name))
  fun isFalling(name: String): Boolean = isFalling(getID(name))
  fun isClear(name: String): Boolean = isClear(getID(name))
  fun getDamagePerSecond(name: String): Int = getDamagePerSecond(getID(name))
  fun getLight(name: String): Int = getLight(getID(name))

  // The OOP raw getter.
  fun get(id: Int): BlockDefinition {
    return BlockDefinition(id, getName(id), getInventoryName(id), getTextures(id), getDrawType(id), isWalkable(id), isLiquid(id), getFlow(id), getViscosity(id), isClimbable(id), isSneakJumpClimbable(id), isFalling(id), isClear(id), getDamagePerSecond(id), getLight(id))
  }
  fun get(name: String): BlockDefinition = get(getID(name))


  // and these are two mini exception factory helpers.
  private fun invalidThrow(name: String, thing: String): RuntimeException {
    return RuntimeException("Tried to get invalid block $name, component $thing")
  }
  private fun invalidThrow(id: Int, thing: String): RuntimeException {
    return RuntimeException("Tried to get invalid block id $id, component $thing")
  }

}

@JvmRecord
data class BlockDefinition(
  val id: Int,
  val name: String,
  val inventoryName: String,
  val textures: Array<String>,
  val drawType: DrawType,
  val walkable: Boolean,
  val liquid: Boolean,
  val flow: Int,
  val viscosity: Int,
  val climbable: Boolean,
  val sneakJumpClimbable: Boolean,
  val falling: Boolean,
  val clear: Boolean,
  val damagePerSecond: Int,
  val light: Int
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BlockDefinition

    if (id != other.id) return false
    if (name != other.name) return false
    if (inventoryName != other.inventoryName) return false
    if (!textures.contentEquals(other.textures)) return false
    if (drawType != other.drawType) return false
    if (walkable != other.walkable) return false
    if (liquid != other.liquid) return false
    if (flow != other.flow) return false
    if (viscosity != other.viscosity) return false
    if (climbable != other.climbable) return false
    if (sneakJumpClimbable != other.sneakJumpClimbable) return false
    if (falling != other.falling) return false
    if (clear != other.clear) return false
    if (damagePerSecond != other.damagePerSecond) return false
    if (light != other.light) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id
    result = 31 * result + name.hashCode()
    result = 31 * result + inventoryName.hashCode()
    result = 31 * result + textures.contentHashCode()
    result = 31 * result + drawType.hashCode()
    result = 31 * result + walkable.hashCode()
    result = 31 * result + liquid.hashCode()
    result = 31 * result + flow
    result = 31 * result + viscosity
    result = 31 * result + climbable.hashCode()
    result = 31 * result + sneakJumpClimbable.hashCode()
    result = 31 * result + falling.hashCode()
    result = 31 * result + clear.hashCode()
    result = 31 * result + damagePerSecond
    result = 31 * result + light
    return result
  }
}


/**
 * This object's soul purpose is to parse and map the block_cache.json file for the BlockDefinitionContainer to get
 * existing block values, or to assign new block values!
 * IDs are stored numerically, 0,1,2,3,4, etc. If there is a gap then there is a problem!
 *!FIXME: This could be slow if there are thousands of blocks, consider using a database!
 */
private const val cacheFolder = "./cache"
private const val cacheFile = "./cache/block_cache.json"
internal object blockIDCache {

  private val nameToIDMap = HashMap<String, Int>()
  // 0 is reserved for air.
  private var nextFreeSlot = 1


  fun initialize() {
    folderCheck()
    fileCheck()
  }

  fun assign(name: String): Int {
    if (name.lowercase() == "air" && !nameToIDMap.containsKey("air")) {
      nameToIDMap[name.lowercase()] = 0
    }
    return getID(name)
  }
  private fun folderCheck() {
    if (!isFolder(cacheFolder)) {
      println("blockIDCache: Creating cache folder.")
      makeFolder(cacheFolder)
    } else {
      println("blockIDCache: Cache folder exists.")
    }
  }

  private fun fileCheck() {
    if (isFile(cacheFile)) {
      println("blockIDCache: Parsing cache file.")
      try { processJSONNodes(ObjectMapper().readTree(getFileString(cacheFile))) } catch (e: Exception) { throw RuntimeException("blockIDCache: $e") }
    }
  }

  private fun processJSONNodes(nodes: JsonNode) {
    // Crawl the JSON tree.

    val keys: MutableIterator<String> = nodes.fieldNames() ?: throw RuntimeException("blockIDCache: Failed to get JSON keys.")

    val test: MutableIterator<JsonNode> = nodes.elements() ?: throw RuntimeException("blockIDCache: Failed to get JSON elements.")

    keys.asSequence().zip(test.asSequence()).forEach {
      val (key, value) = it

      val type = value.nodeType

      if (type != JsonNodeType.NUMBER) {
        throw RuntimeException("blockIDCache: $key was type $type. Did you modify the cache?")
      }

      duplicateCheck(key)

      val rawValue = value.asDouble()
      val numericValue = value.asInt()

      if ((rawValue * 100.0).toInt() != (numericValue * 100)) {
        throw RuntimeException("blockIDCache: $key was floating. Did you modify the cache?")
      }

      // Automatically tick up the free slot for the next numeric value.
      //! fixme: testme: this should automatically do this?
      //! or, this should be ticked up to the next val with a gap check.
      //! or, this should test the gotten value with the expected next value.
      //! this needs to be tested with a broken cache!
      if (numericValue >= nextFreeSlot) {
        nextFreeSlot = numericValue + 1
      }

      nameToIDMap[key] = numericValue
    }
  }

  private fun getID(name: String): Int {
    return nameToIDMap[name] ?: tickUp(name)
  }

  private fun tickUp(name: String): Int {
    val newID = nextFreeSlot
    nameToIDMap[name] = newID
    nextFreeSlot++
    return newID
  }

  private fun duplicateCheck(name: String) {
    if (nameToIDMap.contains(name)) {
      throw RuntimeException("blockIDCache: Duplicate detected. $name")
    }
  }

  fun write() {
    println("blockIDCache: Writing cache.")
    val mapper = ObjectMapper()
    try { mapper.writeValue(makeFile(cacheFile), nameToIDMap) } catch (e: Exception) { throw RuntimeException("blockIDCache: Write error. $e") }
    println("blockIDCache: Cache write successful.")
  }
}

