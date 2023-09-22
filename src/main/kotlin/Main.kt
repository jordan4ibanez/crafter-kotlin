import engine.gl
import engine.glfw
import engine.window

fun main(args: Array<String>) {
  println(args)

  initialization()
  gameLoop()
  destruction()

}

fun initialization() {
  glfw.initialize()
  gl.initialize()
}

fun mainLogic() {

}

fun destruction() {
  glfw.destroy()
}


tailrec fun gameLoop() {
  mainLogic()

  if (window.shouldClose()){
    return
  }
  return gameLoop()
}