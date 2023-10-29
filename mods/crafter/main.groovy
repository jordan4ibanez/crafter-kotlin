package crafter

import engine.api

final api = api.INSTANCE

//api.registerOnTick { float delta ->
//  println("delta is $delta")
//}

["blocks", "entities"].forEach {
  api.dofile("crafter/$it")
}