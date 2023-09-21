import engine.gl
import engine.glfw

fun main(args: Array<String>) {

  println(args)

  glfw.initialize()

  gl.printHi()


  glfw.destroy()

}