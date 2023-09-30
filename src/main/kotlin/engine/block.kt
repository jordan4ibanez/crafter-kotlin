package engine

import java.util.concurrent.ConcurrentHashMap

/*
A concurrent component system to handle blocks.
Basic functional interface.
Indexed by: Name literal OR translate through ID -> name literal
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
    id: Int,
    name: String,
    inventoryName: String,
    textures: Array<String>,
    drawType: DrawType = DrawType.BLOCK
  ) {
    setBlockID(name, id)
    setBlockInventoryName(id, inventoryName)
    setTextures(id, textures)
    setDrawType(id, drawType)
    setInternalName(id, name)
    //todo: Cannot put texture coords here. Need to be generated first
  }

  private fun setInternalName(id: Int, internalName: String) {
    name[id] = internalName
  }

  fun setBlockID(name: String, newId: Int) {
    id[name] = newId
  }

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

