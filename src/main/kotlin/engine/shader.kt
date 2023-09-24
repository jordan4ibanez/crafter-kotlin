package engine

import org.lwjgl.opengl.GL20.*
import java.io.File

object shader {

  // note: We do not want to destroy shaders during gameplay. No individual destruction. Only full.

  private val database = HashMap<String, Shader>()


  fun start(name: String) {
    glUseProgram(safeGet(name).programID)
  }

  fun create(name: String, vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String) {
    val shaderObject = Shader(name, vertexSourceCodeLocation, fragmentSourceCodeLocation)
    safePut(name, shaderObject)
  }

  fun exists(name: String): Boolean {
    return database.containsKey(name)
  }

  fun createUniform(shaderName: String, uniformName: String) {
    val shader = safeGet(shaderName)
    val location = glGetUniformLocation(shader.programID, uniformName)
    if (location < 0) throw RuntimeException("shader: Unable to create uniform in shader $shaderName. $uniformName")
    shader.uniforms[uniformName] = location
  }

  fun createUniforms(shaderName: String, uniformNames: Array<String>) {
    val shader = safeGet(shaderName)
    val shaderProgramID = shader.programID
    uniformNames.forEach { uniformName ->
      val location = glGetUniformLocation(shaderProgramID, uniformName)
      if (location < 0) throw RuntimeException("shader: Unable to create uniform in shader $shaderName. $uniformName")
    }
  }

  fun destroyAll() {
    glUseProgram(0)
    database.values.forEach { shader ->
      glDeleteProgram(shader.programID)
    }

  }

  private fun safePut(name: String, shaderObject: Shader) {
    if (database.containsKey(name)) throw RuntimeException("shader: Attempted to overwrite existing shader. $name")
    database[name] = shaderObject
  }

  private fun safeGet(name: String): Shader {
    return database[name] ?: throw RuntimeException("shader: Attempted to index nonexistent shader. $name")
  }
}

private class Shader {
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

private fun createShader(vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String): Int {
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
  val sourceCode = getFileString(sourceCodeLocation)

  val shaderID = glCreateShader(shaderType)

  if (shaderID == 0) {
    val shaderTypeString = if (shaderType == GL_VERTEX_SHADER) "GL_VERTEX_SHADER" else "GL_FRAGMENT_SHADER"
    throw RuntimeException("compileSourceCode: Failed to create shader $shaderTypeString at $sourceCodeLocation")
  }

  glShaderSource(shaderID, sourceCode)

  glCompileShader(shaderID)

  if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
    throw RuntimeException("compileSourceCode: Error compiling. ${glGetShaderInfoLog(shaderID)}")
  }

  glAttachShader(programID, shaderID)

  return shaderID
}

private fun link(programID: Int, vertexID: Int, fragmentID: Int) {
  glLinkProgram(programID)

  if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
    throw RuntimeException("link: Failed to link program. ${glGetProgramInfoLog(programID)}")
  }

  glDetachShader(programID, vertexID)
  glDetachShader(programID, fragmentID)

  //todo: Test this
//  glDeleteShader(vertexID)
//  glDeleteShader(fragmentID)

  glValidateProgram(programID)

  if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
    throw RuntimeException("link: Validation failed. ${glGetProgramInfoLog(programID)}")
  }
}
