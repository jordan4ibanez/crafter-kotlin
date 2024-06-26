package engine.collision_box

import engine.camera.camera
import engine.model.mesh.Mesh
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc

object collisionBox {

  private val min = Vector3f()
  private val max = Vector3f()
  private val noTextureCoords = FloatArray(16)
  private val indices = intArrayOf(
    // Bottom Square
    0, 1, 1, 2, 2, 3, 3, 0,
    // Top square
    4, 5, 5, 6, 6, 7, 7, 4,
    // Sides
    0, 4, 1, 5, 2, 6, 3, 7
  )

  fun draw(pos: Vector3fc, size: Vector2fc) {
    val positions = generatePositions(size)
    val id = Mesh.create3D("cbox", positions, noTextureCoords, indices, "debug.png")
    camera.setObjectMatrix(pos)
    Mesh.drawLines(id)
    Mesh.destroy(id)
  }


  private fun generatePositions(size: Vector2fc): FloatArray {
    min.set(-size.x(), 0f, -size.x())
    max.set(size.x(), size.y(), size.x())
    return floatArrayOf(
      // Bottom square
      min.x, min.y, min.z, // 0
      min.x, min.y, max.z, // 1
      max.x, min.y, max.z, // 2
      max.x, min.y, min.z, // 3
      // Top square
      min.x, max.y, min.z, // 4
      min.x, max.y, max.z, // 5
      max.x, max.y, max.z, // 6
      max.x, max.y, min.z  // 7
    )
  }
}