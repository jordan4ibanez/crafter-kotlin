package engine

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
import javax.script.ScriptContext
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KCallable


object entity {

  // Definition.
  private val def = HashMap<String, Any>()

  // Instances.
  private val genericData = HashMap<String, Any>()
  private val position = HashMap<String, Vector3f>()


  fun registerGeneric(rawObj: ScriptObjectMirror) {

    // Definition is consumed mutably by the rawObj call below.
    val definition = HashMap<String, Any>()

    if (rawObj.isFunction) {
      //todo: note: functional calls via some type of function interface.
      rawObj.call(null, definition)
    }



  }

  class JSEntity {

  }

  private fun convert(rawObj: Any): Any {
    return when (rawObj) {
      is ScriptObjectMirror -> {
        if (rawObj.isArray) {
          val list = ArrayList<Any>()
          rawObj.forEach { _,value ->
            list.add(convert(value))
          }
          list
        } else {
          val map = HashMap<String, Any>()
          rawObj.forEach { key, value ->
            map[key] = convert(value)
          }
          map
        }
      }
      //? note: It is already a java type.
      else -> {
        rawObj
      }
    }
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