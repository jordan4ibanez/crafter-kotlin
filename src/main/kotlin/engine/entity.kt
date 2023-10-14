package engine

import org.joml.Vector3f
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import org.openjdk.nashorn.internal.runtime.ScriptObject
import java.util.Objects

object entity {
  fun register(rawObj: ScriptObjectMirror) {
    if (rawObj["name"] == null) throw RuntimeException("entity: Failed to create. No name.")
    println("entity: registering ${rawObj["name"]}")
    decoratePosition(rawObj)

    val test = ScriptObjectMirror(rawObj)
  }

  fun decoratePosition(rawObj: ScriptObjectMirror) {
    rawObj["position"] = Vector3f()
  }
}