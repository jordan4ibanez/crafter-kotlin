package engine.shader

import engine.file_helpers.getFileString
import org.lwjgl.opengl.GL20.*


fun createShader(vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String): Int {
  val programID = glCreateProgram()

  if (programID == 0) {
    throw RuntimeException("Shader: Failed to create shader program.")
  }

  val vertexID = compileSourceCode(programID, vertexSourceCodeLocation, GL_VERTEX_SHADER)
  val fragmentID = compileSourceCode(programID, fragmentSourceCodeLocation, GL_FRAGMENT_SHADER)

  link(programID, vertexID, fragmentID)

  return programID
}

private fun compileSourceCode(programID: Int, sourceCodeLocation: String, shaderType: Int): Int {
  val sourceCode =
    getFileString(sourceCodeLocation).expect("Shader: $programID. Location: $sourceCodeLocation does not exist.")

  val shaderID = glCreateShader(shaderType)

  if (shaderID == 0) {
    val shaderTypeString = if (shaderType == GL_VERTEX_SHADER) "GL_VERTEXShaderObject" else "GL_FRAGMENTShaderObject"
    throw RuntimeException("compileSourceCode: Failed to create shader $shaderTypeString at $sourceCodeLocation")
  }

  glShaderSource(shaderID, sourceCode)

  glCompileShader(shaderID)

  if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
    throw RuntimeException("compileSourceCode: Error compiling.\n${glGetShaderInfoLog(shaderID)}")
  }

  glAttachShader(programID, shaderID)

  return shaderID
}

private fun link(programID: Int, vertexID: Int, fragmentID: Int) {
  glLinkProgram(programID)

  if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
    throw RuntimeException("link: Failed to link program.\n${glGetProgramInfoLog(programID)}")
  }

  glDetachShader(programID, vertexID)
  glDetachShader(programID, fragmentID)

  //todo: Test this
//  glDeleteShader(vertexID)
//  glDeleteShader(fragmentID)

  glValidateProgram(programID)

  if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
    throw RuntimeException("link: Validation failed.\n${glGetProgramInfoLog(programID)}")
  }
}
