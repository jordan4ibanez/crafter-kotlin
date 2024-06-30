package engine.initialization

import engine.api.Api
import engine.block.BlockIDCache
import engine.gl.Gl
import engine.glfw.Glfw
import engine.shader.shader

fun initialize() {
  println("Initializing Crafter.")

  Glfw.initialize()
  Gl.initialize()

  shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))
  shader.start("main")

  BlockIDCache.initialize()

  Api.initialize()

  BlockIDCache.write()
}