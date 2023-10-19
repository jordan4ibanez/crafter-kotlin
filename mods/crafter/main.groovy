package crafter

import engine.api

final api = api.INSTANCE

["blocks"].forEach {
    api.dofile("crafter/$it")
}