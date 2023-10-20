package crafter

import engine.Mob
import engine.entity
import org.joml.Vector3f
import org.joml.Vector3fc

final entity = entity.INSTANCE

class Pig extends Mob {

  final String name = "crafter:pig"

  Pig(Vector3fc pos) {
    super(pos)
  }

  @Override
  void onStep(float delta) {
    println("Oink, I am a pig")
    println("this pig exists at ${position.x()}, ${position.y()}, ${position.z()}")
  }
}

entity.registerMobSpawner("crafter:pig", (pos) -> {
  return new Pig(pos)
})
