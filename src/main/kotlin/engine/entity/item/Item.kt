package engine.entity.item

import engine.entity.GroovyEntity
import org.joml.Vector3fc

class Item(private var itemName: String, pos: Vector3fc) : GroovyEntity(pos) {

  override val classifier = "item"

  init {
    this.position.set(pos)
  }

  fun getItemName(): String {
    return itemName
  }
}