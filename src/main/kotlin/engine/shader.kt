package engine

import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush


object shader {

  //note: We do not want to destroy shaders during gameplay. No individual destruction. Only full.
  // This is a state machine.

  private val database = HashMap<String, Shader>()

  private lateinit var currentShader: Shader
  private lateinit var currentUniforms: HashMap<String, Int>

  fun start(name: String) {
    currentShader = safeGet(name)
    currentUniforms = currentShader.uniforms
    glUseProgram(currentShader.programID)
  }

  fun create(name: String, vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String) {
    val shaderObject = Shader(name, vertexSourceCodeLocation, fragmentSourceCodeLocation)
    safePut(name, shaderObject)
    // note: This is a micro helper so shader can just be assigned to immediately.
    currentShader = shaderObject
    currentUniforms = currentShader.uniforms
    println("shader: Shader $name created successfully. ID: ${currentShader.programID}")
  }

  fun createUniform(uniformName: String) {
    val location = glGetUniformLocation(currentShader.programID, uniformName)
    if (location < 0) throw RuntimeException("shader: Unable to create uniform in shader ${currentShader.name}. $uniformName")
    currentUniforms[uniformName] = location
    // Debug for now.
    println("shader: Created uniform $uniformName at $location")
  }

  fun createUniforms(uniformNames: Array<String>) {
    val shaderProgramID = currentShader.programID
    uniformNames.forEach { uniformName ->
      val location = glGetUniformLocation(shaderProgramID, uniformName)
      if (location < 0) throw RuntimeException("shader: Unable to create uniform in shader ${currentShader.name}. $uniformName")
      currentUniforms[uniformName] = location
      // Debug for now.
      println("shader: Created uniform $uniformName at $location")
    }
  }

  fun setUniform(name: String, matrix4f: Matrix4fc) {
    val stack: MemoryStack
    try {
      stack = stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(16)
    matrix4f.get(buffer)
    glUniformMatrix4fv(safeUniformGet(name), false, buffer)
    stack.pop()
  }

  fun setUniform(name: String, vector: Vector3fc) {
    val stack: MemoryStack
    try {
      stack = stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(3)
    vector.get(buffer)
    glUniform3fv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun setUniform(name: String, vector: Vector2fc) {
    val stack: MemoryStack
    try {
      stack = stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(2)
    vector.get(buffer)
    glUniform3fv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun setUniform(name: String, value: Float) {
    val stack: MemoryStack
    try {
      stack = stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(1)
    buffer.put(value).flip()
    glUniform1fv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun setUniform(name: String, value: Int) {
    val stack: MemoryStack
    try {
      stack = stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocInt(1)
    buffer.put(value).flip()
    glUniform1iv(safeUniformGet(name), buffer)
    stack.pop()
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

  private fun safeUniformGet(name: String): Int {
    return currentUniforms[name] ?: throw RuntimeException("shader: Attempted to index nonexistent uniform. $name")
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
