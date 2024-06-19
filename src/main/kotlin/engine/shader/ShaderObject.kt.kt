package engine.shader

import engine.createShader

class ShaderObject {

  val name: String
  val programID: Int
  val uniforms = HashMap<String, Int>()

  constructor(name: String, vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String) {
    this.name = name
    programID = createShader(vertexSourceCodeLocation, fragmentSourceCodeLocation)
  }

  // todo: Add uniform things
  // todo: Add uniform things

}