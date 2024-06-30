package engine.block

import engine.texture_atlas.worldAtlas
import java.util.concurrent.ConcurrentHashMap

/**
 * A concurrent component system to handle blocks.
 * Basic functional interface.
 * Indexed by: ID OR translate through name -> ID
 *
 * So if you're looking where raw block representation data is, it's here.
 */
object Block {

  // Required components.
  private val id = ConcurrentHashMap<String, Int>()
  private val name = concurrent<String>()
  private val inventoryName = concurrent<String>()
  private val textures = concurrent<Array<String>>()
  private val textureCoords = concurrent<Array<FloatArray>>()
  private val drawType = concurrent<DrawType>()

  // Optional components.
  //
  // Main thread only.
  private val friction = singleThreaded<Float>()
  private val walkable = singleThreaded<Boolean>()
  private val flow = singleThreaded<Int>()
  private val viscosity = singleThreaded<Int>()
  private val climbable = singleThreaded<Boolean>()
  private val sneakJumpClimbable = singleThreaded<Boolean>()
  private val falling = singleThreaded<Boolean>()
  private val damagePerSecond = singleThreaded<Int>()
  private val floats = singleThreaded<Boolean>()

  // Concurrent
  private val liquid = concurrent<Boolean>()
  private val clear = concurrent<Boolean>()
  private val light = concurrent<Int>()

// note: setter api begins here.

  fun register(
    name: String,
    inventoryName: String,
    textures: Array<String>,
    drawType: DrawType = DrawType.BLOCK
  ) {
    val id = BlockIDCache.assign(name)
    setID(name, id)
    setInventoryName(id, inventoryName)
    setTextures(id, textures)
    setDrawType(id, drawType)
    setInternalName(id, name)
    //todo: Cannot put texture coords here. Need to be generated first
//    println("created block $name at $id with drawtype $drawType and inv name $inventoryName")
  }

  fun register(name: String, inventoryName: String, textures: Array<String>) {
    register(name, inventoryName, textures, DrawType.BLOCK)
  }

  internal fun updateTextureCoords() {
    textures.forEach {
      val output = Array(6) { FloatArray(8) }
      val (id: Int, textureNames: Array<String>) = it
      textureNames.forEachIndexed { outerIndex, textureName ->
        worldAtlas.getQuadOf(textureName).forEachIndexed { innerIndex, value ->
          output[outerIndex][innerIndex] = value
        }
      }
      textureCoords[id] = output
    }
  }

  // Translators
  private fun setInternalName(id: Int, internalName: String) {
    name[id] = internalName
  }

  private fun setID(name: String, newId: Int) {
    id[name] = newId
  }

  // ID oriented.
  fun setInventoryName(id: Int, newName: String) {
    inventoryName[id] = newName
  }

  fun setTextures(id: Int, newTextures: Array<String>) {
    if (newTextures.size != 6) throw RuntimeException("Tried to set block $id textures with ${newTextures.size} size.")
    textures[id] = newTextures
  }

  private fun setTextureCoords(id: Int, newCoords: Array<FloatArray>) {
    textureCoords[id] = newCoords
  }

  fun setDrawType(id: Int, newDrawType: DrawType) {
    drawType[id] = newDrawType
  }

  fun setFriction(id: Int, newFriction: Float) {
    friction[id] = newFriction
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

  fun setFloats(id: Int, doesFloat: Boolean) {
    floats[id] = doesFloat
  }

  // Name oriented
  fun setInventoryName(name: String, newName: String) = setInventoryName(getID(name), newName)
  fun setTextures(name: String, newTextures: Array<String>) = setTextures(getID(name), newTextures)
  private fun setTextureCoords(name: String, newCoords: Array<FloatArray>) = setTextureCoords(getID(name), newCoords)
  fun setDrawType(name: String, newDrawType: DrawType) = setDrawType(getID(name), newDrawType)
  fun setFriction(name: String, newFriction: Float) = setFriction(getID(name), newFriction)
  fun setWalkable(name: String, isWalkable: Boolean) = setWalkable(getID(name), isWalkable)
  fun setLiquid(name: String, isLiquid: Boolean) = setLiquid(getID(name), isLiquid)
  fun setFlow(name: String, flowLevel: Int) = setFlow(getID(name), flowLevel)
  fun setViscosity(name: String, newViscosity: Int) = setViscosity(getID(name), newViscosity)
  fun setClimbable(name: String, isClimbable: Boolean) = setClimbable(getID(name), isClimbable)
  fun setSneakJumpClimbable(name: String, isSneakJumpClimbable: Boolean) =
    setSneakJumpClimbable(getID(name), isSneakJumpClimbable)

  fun setFalling(name: String, isFalling: Boolean) = setFalling(getID(name), isFalling)
  fun setClear(name: String, isClear: Boolean) = setClear(getID(name), isClear)
  fun setDamagePerSecond(name: String, dps: Int) = setDamagePerSecond(getID(name), dps)
  fun setLight(name: String, newLight: Int) = setLight(getID(name), newLight)
  fun setFloats(name: String, doesFloat: Boolean) = setFloats(getID(name), doesFloat)

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
  fun has(id: Int): Boolean {
    return name.containsKey(id)
  }

  fun getInventoryName(id: Int): String {
    return inventoryName[id] ?: throw invalidThrow(id, "id")
  }

  fun getTextures(id: Int): Array<String> {
    return textures[id] ?: throw invalidThrow(id, "textures")
  }

  internal fun getTextureCoords(id: Int): Array<FloatArray> {
    //! Note: This would crash anyways, ignore cast issue.
    //? note: This has no name oriented counterpart.
    return textureCoords[id] ?: invalidThrow(id, "texture coords") as Array<FloatArray>
  }

  fun getDrawType(id: Int): DrawType {
    return drawType[id] ?: throw invalidThrow(id, "drawType")
  }

  // Name oriented
  fun has(name: String): Boolean {
    return id.containsKey(name)
  }

  fun getInventoryName(name: String): String = getInventoryName(getID(name))
  fun getTextures(name: String): Array<String> = getTextures(getID(name))
  fun getDrawType(name: String): DrawType = getDrawType(getID(name))

  //note: Optionals - defaults are set and returned here.

  // ID oriented.
  fun getFriction(id: Int): Float {
    return friction[id] ?: 4f
  }

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

  fun doesFloat(id: Int): Boolean {
    return floats[id] ?: false
  }

  // Name oriented.
  fun getFriction(name: String): Float = getFriction(getID(name))
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
  fun doesFloat(name: String): Boolean = doesFloat(getID(name))

  // The OOP raw getter.
  fun get(id: Int): BlockDefinition {
    return BlockDefinition(
      id,
      getName(id),
      getInventoryName(id),
      getTextures(id),
      getDrawType(id),
      isWalkable(id),
      isLiquid(id),
      getFlow(id),
      getViscosity(id),
      isClimbable(id),
      isSneakJumpClimbable(id),
      isFalling(id),
      isClear(id),
      getDamagePerSecond(id),
      getLight(id),
      doesFloat(id)
    )
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

