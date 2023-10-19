package crafter

import engine.Mob
import engine.entity
import org.joml.Vector3f
import org.joml.Vector3fc

final entity = entity.INSTANCE

class Pig extends Mob {

  Pig() {
    super("pig or something")
  }

  @Override
  void onStep(float delta) {
    println("Oink, I am a pig")
  }
}

entity.testing("crafter:pig", () -> {println("hi, I am a groovy pig")})

