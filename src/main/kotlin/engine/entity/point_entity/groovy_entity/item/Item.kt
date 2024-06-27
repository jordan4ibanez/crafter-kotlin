package engine.entity.point_entity.groovy_entity.item

import engine.entity.point_entity.groovy_entity.GroovyEntity
import org.joml.Vector3fc

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