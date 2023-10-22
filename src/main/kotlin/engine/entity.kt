package engine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.UUID
import kotlin.collections.HashMap


open class PointEntity {

  val position = Vector3f()
  var meshID = 0

  constructor(pos: Vector3fc) {
    this.position.set(pos)
  }

  open fun onStep(dtime: Float) {}
}

class Particle : PointEntity {
  constructor(pos: Vector3fc) : super(pos)
}

open class GroovyEntity : PointEntity {
  // Thanks, GreenXenith!
  open val classifier = "undefined"
  val uuid = UUID.randomUUID().toString()
  private val size = Vector2f()
  private val rotation = Vector3f()
  private val velocity = Vector3f()

  fun getPosition(): Vector3fc = position
  open fun setPosition(newPosition: Vector3fc) {
    position.set(newPosition)
  }

  fun getSize(): Vector2fc = size
  open fun setSize(newSize: Vector2fc) {
    size.set(newSize)
  }

  fun getRotation(): Vector3fc = rotation
  open fun setRotation(newRotation: Vector3fc) {
    rotation.set(newRotation)
  }

  fun getVelocity(): Vector3fc = velocity
  open fun setVelocity(newVelocity: Vector3fc) {
    velocity.set(newVelocity)
  }

  constructor(pos: Vector3fc) : super(pos)
  open fun onSpawn() {}
  open fun onDespawn() {}
}

class Item : GroovyEntity {
  private var itemName: String
  constructor(itemName: String, pos: Vector3fc) : super(pos) {
    this.itemName = itemName
    this.position.set(pos)
  }

  fun getItemName(): String {
    return itemName
  }
}

enum class Mobility {
  Walk,
  Swim,
  Fly,
  Jump
}

enum class Hostility {
  Hostile,
  Neutral,
  Friendly
}


open class Mob : GroovyEntity {
  var hp = 0
  var fallDamage = false
  var mobility = Mobility.Walk
  var lavaSwim = false
  var hostility = Hostility.Neutral

  constructor(pos: Vector3fc) : super(pos)

  open fun onDeath(){}
  open fun onHit() {}
}

open class Player : Mob {
  override val classifier = "player"
  var name: String

  constructor(pos: Vector3fc, name: String) : super(pos) {
    this.name = name
  }
}

object entity {

  // Spawner function containers
  private val genericSpawners = HashMap<String, (Vector3fc) -> GroovyEntity>()
  private val mobSpawners = HashMap<String, (Vector3fc) -> Mob>()

  // Instance containers
  private val mobs = HashMap<String, Mob>()
  private val players = HashMap<String, Player>()



  fun registerMobSpawner(name: String, spawnMechanism: (Vector3fc) -> Mob) {
    //! todo: add environmental vars, what does this mob spawn on, when should it spawn? biome? light level? peaceful mode?
//    val boof: Mob = (blueprint.declaredConstructors[0]!!.newInstance(Vector3f(0f,0f,0f)) as Mob?)!!
//    boof.onStep(getDelta())

    mobSpawners[name] = spawnMechanism


    //todo: remove this, this is prototyping
//    val mechanism = mobSpawners[name] ?: throw RuntimeException("Mob $name does not exist.")
//
//    val testEntity = spawnMob("crafter:pig", Vector3f(1f,2f,3f))
////
//    testEntity.onStep(getDelta())
//    println(testEntity.classifier)
//    println(testEntity.uuid)
  }


  fun spawnMob(name: String, pos: Vector3fc) {
    val spawnMechanism = mobSpawners[name] ?: throw RuntimeException("entity: Can't spawn mob $name, $name doesn't exist.")
    val mob = spawnMechanism(pos)
    println("entity: Storing mob $name at id ${mob.uuid}")
    mobs[mob.uuid] = mob
  }
  fun storeMob(uuid: String) {
    // todo: storing procedure goes here.
    println("todo: implement mob storing procedure.")
    deleteMob(uuid)
  }
  fun deleteMob(uuid: String) {
    println("entity: Deleting mob $uuid")
    mobs.remove(uuid)
  }

  fun addPlayer(player: Player) {
    println("entity: Storing player ${player.name}")
    players[player.name] = player
  }
  fun spawnPlayer(name: String, pos: Vector3fc) {
    val newPlayer = Player(pos, name)
    players[name] = newPlayer
  }
  fun storePlayer(name: String) {
    // todo: storing procedure goes here.
    print("todo: implement player storing procedure.")
    deletePlayer(name)
  }
  fun deletePlayer(name: String) {
    println("entity: Deleting player $name")
    players.remove(name)
  }

}