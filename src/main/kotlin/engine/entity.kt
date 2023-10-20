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

  val generics = HashMap<String, (Vector3fc) -> GroovyEntity>()
  val mobSpawners = HashMap<String, (Vector3fc) -> Mob>()

  val blah: () -> Unit = fun() {
  }

  fun registerMobSpawner(name: String, spawnMechanism: (Vector3fc) -> Mob) {
    //! todo: add environmental vars, what does this mob spawn on, when should it spawn? biome? light level? peaceful mode?
//    val boof: Mob = (blueprint.declaredConstructors[0]!!.newInstance(Vector3f(0f,0f,0f)) as Mob?)!!
//    boof.onStep(getDelta())

    mobSpawners[name] = spawnMechanism


    //todo: remove this, this is prototyping
//    val mechanism = mobSpawners[name] ?: throw RuntimeException("Mob $name does not exist.")
//
//    val testEntity = mechanism(Vector3f(1f,2f,3f))
//
//    testEntity.onStep(getDelta())
  }

//  fun registerGeneric(name: String, blueprint: Class<GroovyEntity>) {
//    generics[name] = blueprint
//  }

  fun spawnMob(name: String, pos: Vector3fc) {
    val spawnMechanism = mobSpawners[name] ?: throw RuntimeException("entity: Can't spawn mob $name, $name doesn't exist.")
    spawnMechanism(pos)
  }

  fun spawnGeneric(name: String) {
    val test = generics[name]!!.constructors[0]
  }


}