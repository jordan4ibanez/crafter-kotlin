import engine.DrawType
import engine.api.api
import engine.block

final block = block.INSTANCE
final api = api.INSTANCE

println("Crafter API is feeling pretty groovy. 8)")

// Air is registered here.
block.register(
        name = "air",
        inventoryName = "air",
        textures = api.stringArrayOf("air.png", "air.png", "air.png", "air.png", "air.png", "air.png"),
        drawtype = DrawType.AIR
)
block.setWalkable("air", false)