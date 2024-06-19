package crafter

import engine.entity.Hostility
import engine.entity.Mob
import engine.entity.Mobility
import engine.entity.entity
import org.joml.Vector3fc

final entity = entity.INSTANCE

class Pig extends Mob {

  final String classifier = "crafter:pig"
  Hostility hostility = Hostility.Friendly

  Pig(Vector3fc pos) {
    super(pos)
  }

  @Override
  void onTick(float delta) {
    println("Oink, I am a pig")
    println("this pig ${this.uuid} exists at ${position.x()}, ${position.y()}, ${position.z()}")
  }
}

entity.registerMobSpawner("crafter:pig", (pos) -> { return new Pig(pos) })

class Squid extends Mob {

  final String classifier = ""

  Mobility mobility = Mobility.Swim
  Hostility hostility = Hostility.Friendly

  Squid(Vector3fc pos) {
    super(pos)
    println("glub glub")
  }
}

entity.registerMobSpawner("crafter:squid", (pos) -> { return new Squid(pos) })