package engine

import java.util.concurrent.ConcurrentHashMap

/*
A concurrent component system to handle blocks.
Basic functional interface.
Indexed by: Name literal OR translate through ID -> name literal
*/

//note: con stands for container.
private fun<T> con(): ConcurrentHashMap<String, T> {
  return ConcurrentHashMap<String, T>()
}

// Required components.
private val id            = con<Int>()
private val name          = ConcurrentHashMap<Int, String>()
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
    id: Int,
    name: String,
    inventoryName: String,
    textures: Array<String>,
    drawType: DrawType = DrawType.BLOCK
  ) {
    setBlockID(name, id)
    setBlockInventoryName(name, inventoryName)
    setTextures(name, textures)
    setDrawType(name, drawType)
    setInternalName(id, name)
    //todo: Cannot put texture coords here. Need to be generated first
  }

  private fun setInternalName(id: Int, internalName: String) {
    name[id] = internalName
  }

  fun setBlockInventoryName(name: String, newName: String) {
    inventoryName[name] = newName
  }

  fun setBlockID(name: String, newId: Int) {
    id[name] = newId
  }

  fun setTextures(name: String, newTextures: Array<String>) {
    if (newTextures.size != 6) throw RuntimeException("Tried to set block $name textures with ${newTextures.size} size.")
    textures[name] = newTextures
  }

  private fun setTextureCoords(name: String, newCoords: HashMap<String, FloatArray>) {
    textureCoords[name] = newCoords
  }

  fun setDrawType(name: String, newDrawType: DrawType) {
    drawType[name] = newDrawType
  }

  fun setWalkable(name: String, isWalkable: Boolean) {
    walkable[name] = isWalkable
  }

  fun setLiquid(name: String, isLiquid: Boolean) {
    liquid[name] = isLiquid
  }

  fun setFlow(name: String, flowLevel: Int) {
    if (!(0..15).contains(flowLevel)) throw RuntimeException("Tried to set block $name with flow level $flowLevel (0..15)")
    flow[name] = flowLevel
  }

  fun setViscosity(name: String, newViscosity: Int) {
    if (!(0..15).contains(newViscosity)) throw RuntimeException("Tried to set block $name with viscosity $newViscosity (0..15)")
    viscosity[name] = newViscosity
  }

  fun setClimbable(name: String, isClimbable: Boolean) {
    climbable[name] = isClimbable
  }

  fun setSneakJumpClimbable(name: String, isSneakJumpClimbable: Boolean) {
    sneakJumpClimbable[name] = isSneakJumpClimbable
  }

  fun setFalling(name: String, isFalling: Boolean) {
    falling[name] = isFalling
  }

  fun setClear(name: String, isClear: Boolean) {
    clear[name] = isClear
  }

  fun setDamagePerSecond(name: String, dps: Int) {
    damagePerSecond[name] = dps
  }

  fun setLight(name: String, newLight: Int) {
    if (!(0..15).contains(newLight)) throw RuntimeException("Tried to set block $name with viscosity $newLight (0..15)")
    light[name] = newLight
  }

// note: getter api starts here.

  // Required
  fun getID(name: String): Int {
    return id[name] ?: throw invalidThrow(name, "id")
  }
  fun getName(id: Int): String {
    return name[id] ?: throw invalidThrow(id, "name")
  }
  // Name oriented
  fun getInventoryName(name: String): String {
    return inventoryName[name] ?: throw invalidThrow(name, "id")
  }
  fun getTextures(name: String): Array<String> {
    return textures[name] ?: throw invalidThrow(name, "textures")
  }
  fun getDrawType(name: String): DrawType {
    return drawType[name] ?: throw invalidThrow(name, "drawType")
  }

  // Optionals - defaults are set here.

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
  fun isWalkable(name: String): Boolean {
    return walkable[name] ?: true
  }
  fun isLiquid(name: String): Boolean {
    return liquid[name] ?: false
  }
  fun getFlow(name: String): Int {
    return flow[name] ?: 0
  }
  fun getViscosity(name: String): Int {
    return viscosity[name] ?: 0
  }
  fun isClimbable(name: String): Boolean {
    return climbable[name] ?: false
  }
  fun isSneakJumpClimbable(name: String): Boolean {
    return sneakJumpClimbable[name] ?: false
  }
  fun isFalling(name: String): Boolean {
    return falling[name] ?: false
  }
  fun isClear(name: String): Boolean {
    return clear[name] ?: false
  }
  fun getDamagePerSecond(name: String): Int {
    return damagePerSecond[name] ?: 0
  }
  fun getLight(name: String): Int {
    return light[name] ?: 0
  }




  // and these are two mini exception factory helpers.
  private fun invalidThrow(name: String, thing: String): RuntimeException {
    return RuntimeException("Tried to get invalid block $name, component $thing")
  }
  private fun invalidThrow(id: Int, thing: String): RuntimeException {
    return RuntimeException("Tried to get invalid block id $id, component $thing")
  }

}

