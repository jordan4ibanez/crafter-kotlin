import engine.api.Api
import engine.block.DrawType
import engine.block.block

final block = block.INSTANCE
final api = Api.INSTANCE

println("Crafter API is feeling pretty groovy. 8)")

// Air is registered here.
block.register(
    name = "air",
    inventoryName = "air",
    textures = api.stringArrayOf("air.png", "air.png", "air.png", "air.png", "air.png", "air.png"),
    drawtype = DrawType.AIR
)
block.setWalkable("air", false)