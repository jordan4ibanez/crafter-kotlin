import engine.DrawType
import engine.block
import engine.api

final block = block.INSTANCE
final api = api.INSTANCE

println("Crafter API is feeling pretty groovy. 8)")

// Air is registered here.
block.register("air", "air", api.stringArrayOf("air.png","air.png","air.png","air.png","air.png","air.png"), DrawType.AIR)