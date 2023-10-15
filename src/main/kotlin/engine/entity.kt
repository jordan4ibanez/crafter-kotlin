package engine

import org.joml.Vector2f
import org.joml.Vector3f
import org.openjdk.nashorn.api.scripting.JSObject
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import org.openjdk.nashorn.internal.objects.Global
import org.openjdk.nashorn.internal.runtime.Context
import org.openjdk.nashorn.internal.runtime.PropertyMap
import org.openjdk.nashorn.internal.runtime.ScriptFunction
import org.openjdk.nashorn.internal.runtime.ScriptObject
import java.lang.Exception
import java.lang.reflect.Type
import java.util.Objects
import java.util.UUID
import javax.script.ScriptContext
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KCallable


object entity {

  // Instance of an entity in game.
  class GenericJavaScriptEntity {
    //? note: The "selfness" of an entity. In JS, `this.` gets replaced with `this.self.` This is purely for unlimited modularity in JS. Do not use outside of it.
    val self = HashMap<String, Any>()
    val position = Vector3f()
    val size = Vector2f()
    private val definitionName: String
    private val entityID: String = UUID.randomUUID().toString()

    constructor(definitionName: String) {
      // Need to be able to get the js functions & mesh somehow, talk to hashmap below.
      this.definitionName = definitionName
    }
  }

  // Definition.
  private val def = HashMap<String, HashMap<String, Any>>()
  private val meshes = HashMap<String, String>()

  // Instances.





  fun registerGeneric(rawObj: ScriptObjectMirror) {

    // Definition is consumed mutably by the rawObj call below.
    val definition = HashMap<String, Any>()

    if (rawObj.isFunction) {
      //todo: note: functional calls via some type of function interface.
      rawObj.call(null, definition)
    }

    if (!definition.containsKey("name")) throw RuntimeException("entity: Entity is missing name.")
    if (definition["name"]!! !is String) throw RuntimeException("entity: Entity name for ${definition["name"]} must be a string.")
    val name = definition["name"]!! as String

    def[name] = definition
//    jsFunctions[name] = rawObj

    println("entity: Registered $name")

    val testingEntity = GenericJavaScriptEntity("crafter:debug")
    testingEntity.self["x"] = 5

//    println(jsFunctions[name]!!)
    (def[name]!!["blah"] as ScriptObjectMirror).call(testingEntity)

    //! note: This is how you run generic functions.
//    (definition["blah"] as ScriptObjectMirror).call(null, )

  }



  fun spawn(name: String) {
//    val test = ScriptObject()
  }

  fun decoratePosition(rawObj: ScriptObjectMirror) {
    rawObj["position"] = Vector3f()
  }
}

//? note: This is from prototyping.
//    decoratePosition(rawObj)
//
////    val test = ScriptObjectMirror(rawObj)
//
//    println("if is: ${rawObj.isExtensible}")
//
//    rawObj.callMember("test", 1)
//
//    rawObj.forEach {
//      val (key, value) = it
//      println("$key, $value")
//      println("val is: ${value.javaClass}")
//      when (value) {
//        is ScriptObjectMirror -> {
//          println("the thing: ${it.value.javaClass}")
//        }
//      }
//    }