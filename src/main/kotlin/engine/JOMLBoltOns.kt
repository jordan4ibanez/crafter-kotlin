package engine

import org.joml.*

/*
JOML's classes have some pretty horrible print and toString(). Here, I fix that.
When you import this, you can just call print() on a vector.
You can pass it in debug info like "Position" or something.
*/


private fun parseInfo(type: String, debugInfo: String? = null): String {
  return if (debugInfo == null) {
    "$type: "
  } else {
    ("$debugInfo: ")
  }
}

private fun assembleVectorComponents(x: Float, y: Float, z: Float? = null, w: Float? = null): String {
  val builder = StringBuilder()
  run iteration@ {
    listOf("x", "y", "z", "w").zip(listOf(x, y, z, w)).forEach {
      val (key, value) = it
      if (value == null) {
        return@iteration
      }
      if (key != "x") {
        builder.append(", ")
      }
      builder.append("$key: $value")
    }
  }
  return builder.toString()
}

private fun Vector2fc.destructure(): String {
  return assembleVectorComponents(this.x(), this.y())
}
private fun Vector3fc.destructure(): String {
  return assembleVectorComponents(this.x(), this.y(), this.z())
}
private fun Vector4fc.destructure(): String {
  return assembleVectorComponents(this.x(), this.y(), this.z(), this.w())
}

private fun finalizeVectorSerialization(type: String, debugInfo: String?, componentView: String): String {
  return parseInfo(type, debugInfo) + "($componentView)"
}

// Mutable Vector objects inherit from READ-ONLY view. :)
// We add the additional types to make it nicer. You can see I don't need to create secondary pass-throughs. Nice.
fun Vector2fc.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector2fc", debugInfo, this.destructure())
  println(result)
}
fun Vector2f.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector2f", debugInfo, this.destructure())
  println(result)
}

fun Vector3fc.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector3fc", debugInfo, this.destructure())
  println(result)
}
fun Vector3f.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector3f", debugInfo, this.destructure())
  println(result)
}

fun Vector4fc.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector4fc", debugInfo, this.destructure())
  println(result)
}
fun Vector4f.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector4f", debugInfo, this.destructure())
  println(result)
}
