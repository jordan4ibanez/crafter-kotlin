import engine.*

// Initialization procedure. Consider this love.load()
fun load() {

}

// All general logic goes here. Consider this love.update()
fun update() {

}

// All draw procedures go here. Consider this love.draw()
fun draw() {

}

// Game cleanup procedures go here. Consider this love.quit()
fun quit() {

}














// Warning: gameLoop really should not be touched. Focus on the infrastructure around it before adding to it.
tailrec fun gameLoop() {
  window.update()

  update()

  draw()

  window.swapBuffers()

  if (window.shouldClose()) return

  return gameLoop()
}

//note: main is at the bottom because procedures should be put into the designated functions.
// Try not to modify this. It's the literal base of the entire program.
fun main(args: Array<String>) {
  println(args)

  glfw.initialize()
  gl.initialize()

  load()

  gameLoop()

  quit()

  mesh.destroyAll()
  texture.destroyAll()
  glfw.destroy()

}