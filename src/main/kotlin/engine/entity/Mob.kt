package engine.entity

import engine.joml_bolt_ons.print
import org.joml.Vector2f
import org.joml.Vector3fc


open class Mob(pos: Vector3fc) : GroovyEntity(pos) {

  var hp = 0
  var fallDamage = false
  var locomotion = Locomotion.Walk
  var lavaSwim = false
  var hostility = Hostility.Neutral
  var eyeHeight = 1.5f

  open fun onDeath() {}
}

open class Player(pos: Vector3fc, var name: String) : Mob(pos) {

  override val classifier = "player"

  init {
    this.setSize(Vector2f(0.3f, 1.8f))
    this.getSize().print("$name size")
  }
}