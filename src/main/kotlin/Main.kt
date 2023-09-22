import engine.gl
import engine.glfw
import engine.window
import org.joml.Math

fun main(args: Array<String>) {
  println(args)

  initialization()

  gameLoop()

  print(Math.clamp(0f, 1f, -1f))

  destruction()

}

// Initialization procedure. Consider this love.load()
fun initialization() {
  glfw.initialize()
  gl.initialize()
}

// All general logic goes here. Consider this love.update()
fun logic() {

}

// All draw procedures go here. Consider this love.draw()
fun draw() {

}

fun destruction() {
  glfw.destroy()
}


tailrec fun gameLoop() {
  window.update()
  logic()
  draw()
  window.swapBuffers()

  if (window.shouldClose()){
    return
  }
  return gameLoop()
}