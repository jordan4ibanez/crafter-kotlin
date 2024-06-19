package crafter

import engine.api.api
import engine.block
import engine.blockManipulator
import org.joml.Vector3i
import org.joml.Vector3ic

final api = api.INSTANCE
final blockManipulator = blockManipulator.INSTANCE
final block = block.INSTANCE

boolean payloaded = false

api.registerOnTick { float delta ->
    if (!payloaded) {
        final int airID = block.getID("air")
        final int iceID = block.getID("crafter:ice")
        final Vector3ic min = new Vector3i(-31, 0, -31)
        final Vector3ic max = new Vector3i(32, 127, 32)
        if (blockManipulator.set(min, max)) {
//      println("it wurk")
            payloaded = true
            for (x in min.x()..max.x()) {
                for (z in min.z()..max.z()) {
                    for (y in min.y()..max.y()) {
                        if (blockManipulator.getID(x, y, z) != airID) {
                            blockManipulator.setID(x, y, z, iceID)
                        }
                    }
                }
            }
            blockManipulator.write()
        }
    }
}

["blocks", "entities"].forEach {
    api.dofile("crafter/$it")
}