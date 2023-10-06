package engine

fun initialize() {
  println("Initializing Crafter.")

  glfw.initialize()
  gl.initialize()

  shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))
  shader.start("main")

  blockIDCache.initialize()

  api.initialize()

}