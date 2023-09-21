import engine.glfw.glfw
import engine.print
import org.joml.Vector2f

fun main(args: Array<String>) {
  glfw.initialize()

  run {
    val monitorSize = glfw.getMonitorSize()
//    println("Monitor Size: $monitorSize")
  }

  val hi = Vector2f(0f,1f)

  hi.print("Test Thing")

}