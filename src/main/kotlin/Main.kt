import engine.gl
import engine.glfw
import engine.texture
import engine.window

fun main(args: Array<String>) {
  println(args)

  load()

  gameLoop()

  quit()

}

// Initialization procedure. Consider this love.load()
fun load() {
  glfw.initialize()
  gl.initialize()
}

// All general logic goes here. Consider this love.update()
fun update() {

}

// All draw procedures go here. Consider this love.draw()
fun draw() {

}

// Game cleanup procedures go here. Consider this love.quit()
fun quit() {
  texture.destroyAll()
  glfw.destroy()
}


tailrec fun gameLoop() {
  window.update()

  update()

  draw()

  window.swapBuffers()

  if (window.shouldClose()) return

  return gameLoop()
}