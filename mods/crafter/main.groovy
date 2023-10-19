package crafter

import org.joml.Vector3f
import engine.api

final api = api.INSTANCE

final String currentDir = api.getCurrentModDirectory()

["blocks"].forEach {
    api.dofile("crafter/$it")
}