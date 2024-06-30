package engine.initialization

import engine.api.Api
import engine.block.BlockIDCache
import engine.gl.Gl
import engine.glfw.glfw
import engine.shader.shader

fun initialize() {
  println("Initializing Crafter.")

  glfw.initialize()
  Gl.initialize()

  shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))
  shader.start("main")

  BlockIDCache.initialize()

  Api.initialize()

  BlockIDCache.write()
}