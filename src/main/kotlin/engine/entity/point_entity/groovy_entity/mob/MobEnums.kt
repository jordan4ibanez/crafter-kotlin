package engine.entity.point_entity.groovy_entity.mob

/**
 * How a Mob moves around.
 */
enum class Locomotion {

  Walk,
  Swim,
  Fly,
  Jump
}

/**
 * A Mob's base behavior towards the player.
 */
enum class Hostility {

  Hostile,
  Neutral,
  Friendly
}
