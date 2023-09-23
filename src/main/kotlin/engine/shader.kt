package engine

import org.lwjgl.opengl.GL20.glCreateProgram

object shader {

}

private class Shader {
  val name: String
  val id: Int
  val uniforms = HashMap<String, Int>()

  constructor(name: String, vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String) {
    this.name = name
    id = createShader()
  }

}

private fun createShader(): Int {
  val id = glCreateProgram()

  if (id == 0) {
    throw RuntimeException("Shader: Failed to create shader program.")
  }

  //todo compilation goes here


  //todo linkage goes here

  return id
}

private fun compileSourceCode()