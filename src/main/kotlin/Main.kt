import engine.*

// Initialization procedure. Consider this love.load()
fun load() {

  glfw.initialize()
  gl.initialize()
  shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))

  // Debug texture.
  texture.create("debug", "./textures/debug.png")

  // Debug mesh.
  mesh.create3D(
    "debug",
    // positions.
    floatArrayOf(
      -1f, -1f, 0f,
      1f, -1f, 0f
      -1f, -1f, 0f
    ),
    // texture coords.
    floatArrayOf(0f, 0f, 0f),
    // indices.
    intArrayOf(0, 1, 2),
    // texture name.
    "debug"
    )

}

var timer = 0f
var counter = 0
var color = 0f
var brighten = true
var speed = 0.5f

// All general logic goes here. Consider this love.update()
fun update(dtime: Float) {

  timer += dtime

  if (timer >= 1f) {
    timer -= 1f
    window.setTitle("Count: ${counter++}")
  }

  if (brighten) {
    color += dtime * speed
    if (color >= 1f) {
      color = 1f
      brighten = false
    }
  } else {
    color -= dtime * speed
    if (color <= 0f) {
      color = 0f
      brighten = true
    }
  }
  window.setClearColor(color)

//  println(color)


}

// All draw procedures go here. Consider this love.draw()
fun draw() {

}

// Game cleanup procedures go here. Consider this love.quit()
fun quit() {

  mesh.destroyAll()
  texture.destroyAll()
  shader.destroyAll()
  glfw.destroy()

}














// Warning: gameLoop really should not be touched. Focus on the infrastructure around it before adding to it.
tailrec fun gameLoop() {
  window.update()

  update(getDelta())

  draw()

  window.swapBuffers()

  if (window.shouldClose()) return

  return gameLoop()
}

//note: main is at the bottom because procedures should be put into the designated functions.
// Try not to modify this. It's the literal base of the entire program.
fun main(args: Array<String>) {
  println(args)

  load()

  gameLoop()

  quit()

}