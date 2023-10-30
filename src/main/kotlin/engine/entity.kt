package engine

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.UUID
import kotlin.collections.HashMap

const val interpolationSnappiness = 20f

open class PointEntity {

  internal var interpolationTimer = 0f
  internal val oldPosition = Vector3f()
  internal val interpolationPosition = Vector3f()
  val position = Vector3f()
  private val velocity = Vector3f()
  var meshID = 0
  var onGround = false

  constructor(pos: Vector3fc) {
    this.position.set(pos)
    oldPosition.set(pos)
    interpolationPosition.set(pos)
  }

  fun getPosition(): Vector3fc = position
  open fun setPosition(newPosition: Vector3fc) {
    oldPosition.set(position)
    position.set(newPosition)
    interpolationTimer = 0f
  }
  internal fun interpolate(delta: Float) {
    if (interpolationTimer >= 1f) return
//    println("interpolating thing $interpolationTimer")
    interpolationTimer += delta * interpolationSnappiness
    if (interpolationTimer >= 1f) interpolationTimer = 1f
    oldPosition.lerp(position, interpolationTimer, interpolationPosition)
  }

  fun getVelocity(): Vector3fc = velocity
  open fun setVelocity(newVelocity: Vector3fc) {
    velocity.set(newVelocity)
  }
  fun addVelocity(moreVelocity: Vector3fc) {
    velocity.add(moreVelocity)
  }
  fun addVelocity(x: Float, y: Float, z: Float) {
    velocity.add(x,y,z)
  }
  
  open fun onTick(delta: Float) {}
}

class Particle : PointEntity {
  constructor(pos: Vector3fc) : super(pos)
}

open class GroovyEntity : PointEntity {
  // Thanks, GreenXenith!
  open val classifier = "undefined"
  val uuid = UUID.randomUUID().toString()
  private val size = Vector2f(1f,1f)
  private val rotation = Vector3f()

  fun drawCollisionBox() {
    collisionBox.draw(interpolationPosition, size)
  }

  fun getSize(): Vector2fc = size
  open fun setSize(newSize: Vector2fc) {
    size.set(newSize)
  }

  fun getRotation(): Vector3fc = rotation
  open fun setRotation(newRotation: Vector3fc) {
    rotation.set(newRotation)
  }

  constructor(pos: Vector3fc) : super(pos)
  open fun onSpawn() {}
  open fun onDespawn() {}
  open fun onHit() {}
  open fun onRightClick() {}

}

class Item : GroovyEntity {
  private var itemName: String
  override val classifier = "item"
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
  var eyeHeight = 1.5f

  constructor(pos: Vector3fc) : super(pos)

  open fun onDeath(){}
}

open class Player : Mob {
  override val classifier = "player"
  var name: String

  constructor(pos: Vector3fc, name: String) : super(pos) {
    this.name = name
    setSize(Vector2f(0.3f, 1.8f))
    getSize().print("$name size")
  }
}

object entity {

  // Spawner function containers
  private val genericSpawners = HashMap<String, (Vector3fc) -> GroovyEntity>()
  private val mobSpawners = HashMap<String, (Vector3fc) -> Mob>()

  // Instance containers
  //
  // Generic - GroovyEntity
  private val generics = HashMap<String, GroovyEntity>()
  //
  // Focused - Mob, Item, etc
  private val mobs = HashMap<String, Mob>()
  private val players = HashMap<String, Player>()
  //
  // Specialty
  private val particles = HashMap<String, Particle>()

  fun doOnStep(delta: Float) {
    generics.forEach { (key, obj) ->
      obj.interpolate(delta)
    }
  }

  fun doOnTick(delta: Float) {
    generics.forEach { (key, obj) ->
      collision.collideEntityToWorld(obj)
      // todo: collision result can be set from collide entity to entities
      obj.onTick(delta)
    }
  }

  fun draw() {
    generics.forEach {(key, obj) ->
      obj.drawCollisionBox()
    }
  }


  //todo: particles need a special instantiation thing.

  fun registerMobSpawner(name: String, spawnMechanism: (Vector3fc) -> Mob) {
    //! todo: add environmental vars, what does this mob spawn on, when should it spawn? biome? light level? peaceful mode?
    mobSpawners[name] = spawnMechanism
  }

//  fun getAllEntities(): Map<String, Mob> {
//    return mobs.plus(players)
//  }


  fun spawnMob(name: String, pos: Vector3fc) {
    val spawnMechanism = mobSpawners[name] ?: throw RuntimeException("entity: Can't spawn mob $name, $name doesn't exist.")
    val mob = spawnMechanism(pos)
    println("entity: Storing mob $name at id ${mob.uuid}")

    mobs[mob.uuid] = mob
    addGeneric(mob.uuid, mob)
  }
  fun storeMob(uuid: String) {
    // todo: storing procedure goes here.
    println("todo: implement mob storing procedure.")
    deleteMob(uuid)
  }
  fun deleteMob(uuid: String) {
    println("entity: Deleting mob $uuid")
    mobs.remove(uuid)
    deleteGeneric(uuid)
  }
  fun hasMob(uuid: String): Boolean {
    return mobs.containsKey(uuid)
  }
  fun getMob(uuid: String): Mob {
    return mobs[uuid]!!
  }

  fun addPlayer(player: Player) {
    players[player.name] = player
    println("entity: Added player ${player.name}")
    addGeneric(player.name, player)
  }
  fun spawnPlayer(name: String, pos: Vector3fc) {
    val newPlayer = Player(pos, name)
    players[name] = newPlayer
    println("entity: Spawned player $name")
    addGeneric(name, newPlayer)
  }
  fun storePlayer(name: String) {
    // todo: storing procedure goes here.
    print("todo: implement player storing procedure. $name")
    deletePlayer(name)
  }
  fun deletePlayer(name: String) {
    println("entity: Deleting player $name")
    players.remove(name)
    generics.remove(name)
  }
  fun hasPlayer(name: String): Boolean {
    return players.containsKey(name)
  }
  fun getPlayer(name: String): Player {
    return players[name]!!
  }

  private fun addGeneric(name: String, generic: GroovyEntity) {
    generics[name] = generic
    println("entity: Stored generic $name")
  }
  private fun deleteGeneric(name: String) {
    generics.remove(name)
    println("entity: Deleted generic $name")
  }

}

//todo: remove this, this is prototyping
//    val boof: Mob = (blueprint.declaredConstructors[0]!!.newInstance(Vector3f(0f,0f,0f)) as Mob?)!!
//    boof.onStep(getDelta())

//    val mechanism = mobSpawners[name] ?: throw RuntimeException("Mob $name does not exist.")
//
//    val testEntity = spawnMob("crafter:pig", Vector3f(1f,2f,3f))
////
//    testEntity.onStep(getDelta())
//    println(testEntity.classifier)
//    println(testEntity.uuid)