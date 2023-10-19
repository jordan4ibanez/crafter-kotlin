package crafter

import engine.api

final api = api.INSTANCE

["blocks", "entities"].forEach {
    api.dofile("crafter/$it")
}