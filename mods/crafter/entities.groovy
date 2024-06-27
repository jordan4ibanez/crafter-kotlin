package crafter

import engine.entity.EntityHandler
import engine.entity.mob.Hostility
import engine.entity.mob.Locomotion
import engine.entity.mob.Mob
import org.jetbrains.annotations.NotNull
import org.joml.Vector3fc

final entity = EntityHandler.INSTANCE

class Pig extends Mob {

  final String classifier = "crafter:pig"
  Hostility hostility = Hostility.Friendly

  Pig(@NotNull Vector3fc pos) {
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

  Locomotion locomotion = Locomotion.Swim
  Hostility hostility = Hostility.Friendly

  Squid(@NotNull Vector3fc pos) {
    super(pos)
    println("glub glub")
  }
}

entity.registerMobSpawner("crafter:squid", (pos) -> { return new Squid(pos) })