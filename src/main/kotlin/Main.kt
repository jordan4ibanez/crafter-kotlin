import engine.gl
import engine.glfw
import engine.texture
import engine.window
import org.joml.Math

fun main(args: Array<String>) {
  println(args)

  initialization()

  gameLoop()

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
  texture.destroyAll()
  glfw.destroy()
}


tailrec fun gameLoop() {
  window.update()
  logic()
  draw()
  window.swapBuffers()

  if (window.shouldClose()) return

  return gameLoop()
}