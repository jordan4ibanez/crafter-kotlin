package crafter

import engine.DrawType
import engine.block
import engine.api

final api = api.INSTANCE
final block = block.INSTANCE



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
