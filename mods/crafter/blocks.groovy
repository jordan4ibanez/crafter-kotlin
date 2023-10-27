package crafter

import engine.block
import engine.api
import engine.blockManipulator

final api = api.INSTANCE
final block = block.INSTANCE
final blockManipulator = blockManipulator.INSTANCE


block.register(
  name = "crafter:stone",
  inventoryName = "Stone",
  textures = api.stringArrayOf("stone.png","stone.png","stone.png","stone.png","stone.png","stone.png")
)

block.register(
  name = "crafter:dirt",
  inventoryName = "Dirt",
  textures = api.stringArrayOf("dirt.png","dirt.png","dirt.png","dirt.png","dirt.png","dirt.png"),
)

block.register(
  name = "crafter:grass",
  inventoryName = "Grass",
  textures = api.stringArrayOf("grass.png","grass.png","grass.png","grass.png","dirt.png","grass.png"),
)

block.register(
  name = "crafter:tree",
  inventoryName = "Tree",
  textures = api.stringArrayOf("treeOut.png","treeOut.png","treeOut.png","treeOut.png","treeCore.png","treeCore.png"),
)

block.register(
  name = "crafter:wood",
  inventoryName = "Wood",
  textures = api.stringArrayOf("wood.png","wood.png","wood.png","wood.png","wood.png","wood.png"),
)

block.register(
  name = "crafter:tnt",
  inventoryName = "TNT",
  textures = api.stringArrayOf("tnt_side.png","tnt_side.png","tnt_side.png","tnt_side.png","tnt_bottom.png","tnt_top.png"),
)

block.register(
  name = "crafter:cobblestone",
  inventoryName = "Cobblestone",
  textures = api.stringArrayOf("cobble.png","cobble.png","cobble.png","cobble.png","cobble.png","cobble.png"),
)

block.register(
  name = "crafter:brick",
  inventoryName = "Brick",
  textures = api.stringArrayOf("brick.png","brick.png","brick.png","brick.png","brick.png","brick.png"),
)