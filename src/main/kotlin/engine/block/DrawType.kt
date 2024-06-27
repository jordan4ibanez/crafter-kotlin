package engine.block

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
