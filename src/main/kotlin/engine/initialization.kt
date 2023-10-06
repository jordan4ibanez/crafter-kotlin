package engine

fun initialize() {
  println("Initializing Crafter.")

  glfw.initialize()
  gl.initialize()
  blockIDCache.initialize()

  shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))
  shader.start("main")



  api.initialize()

}