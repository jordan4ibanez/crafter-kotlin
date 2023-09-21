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

private fun Vector2fc.destructureToString(): String {
  return assembleVectorComponents(this.x(), this.y())
}
private fun Vector3fc.destructureToString(): String {
  return assembleVectorComponents(this.x(), this.y(), this.z())
}
private fun Vector4fc.destructureToString(): String {
  return assembleVectorComponents(this.x(), this.y(), this.z(), this.w())
}

private fun finalizeVectorSerialization(type: String, debugInfo: String?, componentView: String): String {
  return parseInfo(type, debugInfo) + "($componentView)"
}

// Mutable Vector objects inherit from READ-ONLY view. :)
// We add the additional types to make it nicer. You can see I don't need to create secondary pass-throughs. Nice.
fun Vector2fc.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector2fc", debugInfo, this.destructureToString())
  println(result)
}
fun Vector2f.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector2f", debugInfo, this.destructureToString())
  println(result)
}

fun Vector3fc.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector3fc", debugInfo, this.destructureToString())
  println(result)
}
fun Vector3f.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector3f", debugInfo, this.destructureToString())
  println(result)
}

fun Vector4fc.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector4fc", debugInfo, this.destructureToString())
  println(result)
}
fun Vector4f.print(debugInfo: String? = null) {
  val result = finalizeVectorSerialization("Vector4f", debugInfo, this.destructureToString())
  println(result)
}

// Again, mutable objects inherit from READ-ONLY views. We can do this trick for real now.
// Destructure is also an alias for toArray(). Which I need to also implement next.
fun Vector2fc.destructure(): Array<Float> {
  return arrayOf(this.x(), this.y())
}
fun Vector2ic.destructure(): Array<Int> {
  return arrayOf(this.x(), this.y())
}

fun Vector3fc.destructure(): Array<Float> {
  return arrayOf(this.x(), this.y(), this.z())
}
fun Vector3ic.destructure(): Array<Int> {
  return arrayOf(this.x(), this.y(), this.z())
}

fun Vector4fc.destructure(): Array<Float> {
  return arrayOf(this.x(), this.y(), this.z(), this.w())
}
fun Vector4ic.destructure(): Array<Int> {
  return arrayOf(this.x(), this.y(), this.z(), this.w())
}

fun Vector2fc.toArray(): Array<Float> {
  return this.destructure()
}
fun Vector2ic.toArray(): Array<Int> {
  return this.destructure()
}

fun Vector3fc.toArray(): Array<Float> {
  return this.destructure()
}
fun Vector3ic.toArray(): Array<Int> {
  return this.destructure()
}

fun Vector4fc.toArray(): Array<Float> {
  return this.destructure()
}
fun Vector4ic.toArray(): Array<Int> {
  return this.destructure()
}