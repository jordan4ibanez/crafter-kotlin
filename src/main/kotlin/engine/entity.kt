package engine

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3fc

open class PointEntity {
  var name = "undefined"
  val position = Vector3f()
  constructor(pos: Vector3fc) {
    this.position.set(pos)
  }
}

open class GroovyEntity : PointEntity {
  val size = Vector2f()
  val rotation = Vector2f()
  constructor(pos: Vector3fc) : super(pos)

  open fun onStep(dtime: Float) {}
  open fun onSpawn() {}
  open fun onDespawn() {}
}

class Item : GroovyEntity {
  var itemName: String
  constructor(itemName: String, pos: Vector3fc) : super(pos) {
    this.itemName = itemName
    this.position.set(pos)
  }
}

open class Mob : GroovyEntity {
  var hp = 0
  var fallDamage = false
  constructor(pos: Vector3fc) : super(pos)

  open fun onDeath(){}
  open fun onHit() {}
}

object entity {

  val generics = HashMap<String, Class<GroovyEntity>>()

  val blah: () -> Unit = fun() {
  }

  fun testing(name: String, spawnMechanism: () -> Unit) {
//    val boof: Mob = (blueprint.declaredConstructors[0]!!.newInstance(Vector3f(0f,0f,0f)) as Mob?)!!
//    boof.onStep(getDelta())

    println("THIS IS GETTING CALLED!")

    spawnMechanism()

  }

  fun registerGeneric(name: String, blueprint: Class<GroovyEntity>) {
    generics[name] = blueprint
  }

  fun spawnGeneric(name: String) {
    val test = generics[name]!!.constructors[0]
  }


}