package engine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f

open class PointEntity {
  var meshName: String
  val position = Vector3f()
  constructor(meshName: String) {
    this.meshName = meshName
  }
}

open class GroovyEntity : PointEntity {
  val size = Vector2f()
  val rotation = Vector2f()
  constructor(meshName: String) : super(meshName)

  fun onStep(dtime: Float) {}
  fun onSpawn() {}
  fun onDespawn() {}
}

class Item : GroovyEntity {
  var itemName: String
  constructor(itemName: String) : super("need to get mesh from item container") {
    this.itemName = itemName
  }
}

open class Mob : GroovyEntity {
  var hp = 0
  var fallDamage = false
  constructor(meshName: String) : super(meshName)

  fun onDeath(){}
  fun onHit() {}
}

object entity {


}