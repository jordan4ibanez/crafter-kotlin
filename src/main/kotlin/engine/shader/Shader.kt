package engine.shader

import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryStack

object Shader {

  //note: We do not want to destroy shaders during gameplay. No individual destruction. Only full.
  // This is a state machine.

  private val database = HashMap<String, ShaderObject>()

  private lateinit var currentShader: ShaderObject
  private lateinit var currentUniforms: HashMap<String, Int>

  fun start(name: String) {
    currentShader = safeGet(name)
    currentUniforms = currentShader.uniforms
    GL20.glUseProgram(currentShader.programID)
  }

  fun create(name: String, vertexSourceCodeLocation: String, fragmentSourceCodeLocation: String) {
    val shaderObject = ShaderObject(name, vertexSourceCodeLocation, fragmentSourceCodeLocation)
    safePut(name, shaderObject)
    // note: This is a micro helper so shader can just be assigned to immediately.
    currentShader = shaderObject
    currentUniforms = currentShader.uniforms
    println("shader: Shader $name created successfully. ID: ${currentShader.programID}")
  }

  fun createUniform(uniformName: String) {
    val location = GL20.glGetUniformLocation(currentShader.programID, uniformName)
    if (location < 0) throw RuntimeException("shader: Unable to create uniform in shader ${currentShader.name}. $uniformName")
    currentUniforms[uniformName] = location
    // Debug for now.
    println("shader: Created uniform $uniformName at $location")
  }

  fun createUniforms(uniformNames: Array<String>) {
    val shaderProgramID = currentShader.programID
    uniformNames.forEach { uniformName ->
      val location = GL20.glGetUniformLocation(shaderProgramID, uniformName)
      if (location < 0) throw RuntimeException("shader: Unable to create uniform in shader ${currentShader.name}. $uniformName")
      currentUniforms[uniformName] = location
      // Debug for now.
      println("shader: Created uniform $uniformName at $location")
    }
  }

  fun setUniform(name: String, matrix4f: Matrix4fc) {
    val stack: MemoryStack
    try {
      stack = MemoryStack.stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(16)
    matrix4f.get(buffer)
    GL20.glUniformMatrix4fv(safeUniformGet(name), false, buffer)
    stack.pop()
  }

  fun setUniform(name: String, vector: Vector3fc) {
    val stack: MemoryStack
    try {
      stack = MemoryStack.stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(3)
    vector.get(buffer)
    GL20.glUniform3fv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun setUniform(name: String, vector: Vector2fc) {
    val stack: MemoryStack
    try {
      stack = MemoryStack.stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(2)
    vector.get(buffer)
    GL20.glUniform3fv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun setUniform(name: String, value: Float) {
    val stack: MemoryStack
    try {
      stack = MemoryStack.stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocFloat(1)
    buffer.put(value).flip()
    GL20.glUniform1fv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun setUniform(name: String, value: Int) {
    val stack: MemoryStack
    try {
      stack = MemoryStack.stackPush()
    } catch (e: Exception) {
      throw RuntimeException("setUniform: Failed to allocate stack memory. $name | ${currentShader.name}")
    }
    val buffer = stack.mallocInt(1)
    buffer.put(value).flip()
    GL20.glUniform1iv(safeUniformGet(name), buffer)
    stack.pop()
  }

  fun destroyAll() {
    GL20.glUseProgram(0)
    database.values.forEach { shader ->
      GL20.glDeleteProgram(shader.programID)
    }
  }

  private fun safePut(name: String, shaderObject: ShaderObject) {
    if (database.containsKey(name)) throw RuntimeException("shader: Attempted to overwrite existing shader. $name")
    database[name] = shaderObject
  }

  private fun safeGet(name: String): ShaderObject {
    return database[name] ?: throw RuntimeException("shader: Attempted to index nonexistent shader. $name")
  }

  private fun safeUniformGet(name: String): Int {
    return currentUniforms[name] ?: throw RuntimeException("shader: Attempted to index nonexistent uniform. $name")
  }
}