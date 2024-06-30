package engine.initialization

import engine.api.Api
import engine.block.BlockIDCache
import engine.gl.Gl
import engine.glfw.Glfw
import engine.shader.Shader

fun initialize() {
  println("Initializing Crafter.")

  Glfw.initialize()
  Gl.initialize()

  Shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  Shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))
  Shader.start("main")

  BlockIDCache.initialize()

  Api.initialize()

  BlockIDCache.write()
}