package engine.block

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
  val light: Int,
  val floats: Boolean
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
    if (floats != other.floats) return false

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