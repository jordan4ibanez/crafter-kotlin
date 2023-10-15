package engine

import org.joml.Vector2f
import org.joml.Vector3f
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.*


object entity {

  /*
  If you can understand what's going on here, you deserve a medal.
  Basically, this is a 4D game of chess, but with data.
  JS creates the object which we are not only using raw, but we're using processed.
  Kotlin can talk to the JS object to run methods via it's method calls.
  But we are using the anonymous object as a class definition to treat the GenericJavaScriptEntity as a true generic.
  A GJSE can literally transfer entity defs on the fly, it is completely unbound. This is to allow JS scripting to do some absolutely crazy shit.

  This is why the API transmogrifies `this.` to `this.self.`
  In JS your methods aren't talking to JS objects, they're talking to kotlin JVM native objects!
  */

  // Definition.
  private val def = HashMap<String, HashMap<String, Any>>()
  private val meshes = HashMap<String, String>()

  // Instances.

  // Instance of an entity in game.
  class GenericJavaScriptEntity {
    //? note: The "selfness" of an entity. In JS, `this.` gets replaced with `this.self.` This is purely for unlimited modularity in JS. Do not use outside of JS unless you like crashes.
    val self = HashMap<String, Any>()
    val position = Vector3f()
    val size = Vector2f()
    private val definitionName: String
    private val entityID: String = UUID.randomUUID().toString()

    constructor(definitionName: String) {
      if (!def.containsKey(definitionName)) throw RuntimeException("GenericJavaScriptEntity: Created an undefined entity.")
      // Need to be able to get the js functions & mesh somehow, talk to hashmap below.
      this.definitionName = definitionName
    }

    fun executeDefMethodIfExists(method: String, vararg args: Any) {
      val currentDef = def[definitionName] ?: throw RuntimeException("GenericJavaScriptEntity: Definition $definitionName was somehow deleted.")
      //? note: * is the spread operator. Ant matcher. Kotlin vararg -> Java vararg, basically.
      with (currentDef[method] ?: return println("GenericJavaScriptEntity: Method $method does not exist, skipping.")) {
        when {
          this is ScriptObjectMirror -> this.call(this, *args)
          else -> println("GenericJavaScriptEntity: $method is data, not a method. Skipping.")
        }
      }
    }
  }


  fun registerGeneric(rawObj: ScriptObjectMirror) {

    // Definition is consumed mutably by the rawObj call below.
    val definition = HashMap<String, Any>()

    if (rawObj.isFunction) {
      //todo: note: functional calls via some type of function interface.
      rawObj.call(null, definition)
    } else {
      throw RuntimeException("registerGeneric: Don't modify the JavaScript API. https://youtu.be/PozOqxEWnCo")
    }

    if (!definition.containsKey("name")) throw RuntimeException("entity: Entity is missing name.")
    if (definition["name"]!! !is String) throw RuntimeException("entity: Entity name for ${definition["name"]} must be a string.")
    val name = definition["name"]!! as String

    def[name] = definition

    println("entity: Registered $name")

//    val testingEntity = GenericJavaScriptEntity("crafter:debug")
//    testingEntity.self["x"] = 5
//
//    testingEntity.executeDefMethodIfExists("onStep", getDelta())

    //! note: This is how you run generic functions.
//    (definition["blah"] as ScriptObjectMirror).call(null, )

  }


  fun spawn(name: String) {
//    val test = ScriptObject()
    val definition = def[name]!!
    definition.forEach { key, value ->
      when (value) {
        is ScriptObjectMirror -> {
          // check if function here
        }
        else -> {
          // add default data here
        }
      }
    }
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