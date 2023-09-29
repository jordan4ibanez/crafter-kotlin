import engine.*
import kotlinx.coroutines.*
import org.joml.Math.random
import org.joml.Math.toRadians
import org.joml.Random
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_KEY_R
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

// Initialization procedure. Consider this love.load()
fun load() {


  glfw.initialize()
  gl.initialize()
  shader.create("main", "./shaders/main_shader.vert", "./shaders/main_shader.frag")
  shader.createUniforms(arrayOf("cameraMatrix", "objectMatrix"))
  shader.start("main")

  // Debug texture.

  worldAtlas.add("1", "./textures/debug.png")
  worldAtlas.add("blah", "./textures/text_box.png")
  worldAtlas.add("fkalj", "./textures/test_thing.png")

  texture.create("debug", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())

  // Debug mesh.
  mesh.create3D(
    "debug",
    // positions.
    floatArrayOf(
      -0.5f,  0.5f, 0f,
      -0.5f, -0.5f, 0f,
       0.5f, -0.5f, 0f,
       0.5f,  0.5f, 0f
    ),
    // texture coords.
    floatArrayOf(
      0f, 0f,
      0f, 1f,
      1f, 1f,
      1f, 0f
    ),
    // indices.
    intArrayOf(0, 1, 2, 0, 2, 3),
    // texture name.
    "debug"
    )

}




var timer = 0f
var counter = 0
var color = 0f
var brighten = true
var speed = 0.5f

// All general logic goes here. Consider this love.update()
fun update(dtime: Float) {

  if (keyboard.isDown(GLFW_KEY_ESCAPE)){
   window.close()
    return
  }

  if (keyboard.isPressed(GLFW_KEY_R)) {
    regenerateWorldAtlas()
  }

  timer += dtime

  if (timer >= 1f) {
    timer -= 1f
    window.setTitle("Count: ${counter++}")
  }

  if (brighten) {
    color += dtime * speed
    if (color >= 1f) {
      color = 1f
      brighten = false
    }
  } else {
    color -= dtime * speed
    if (color <= 0f) {
      color = 0f
      brighten = true
    }
  }
  window.setClearColor(color)

//  println(color)


}

fun regenerateWorldAtlas() {

  worldAtlas.clear()
  worldAtlas.add("1", "./textures/debug.png")
  worldAtlas.add("blah", "./textures/text_box.png")
  worldAtlas.add("fkalj", "./textures/test_thing.png")

  texture.destroy("debug")
  texture.create("debug", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())
  mesh.swapTexture("debug", "debug")
}



// All draw procedures go here. Consider this love.draw()
var rotation = 0f
fun draw() {
//  rotation += getDelta() * 50f
//  println(rotation)
  camera.updateCameraMatrix()
  camera.setObjectMatrix(Vector3f(0f, 0f, -1f), Vector3f(0f,toRadians(rotation), 0f))
  mesh.draw("debug")

}




// Game cleanup procedures go here. Consider this love.quit()
fun quit() {

  mesh.destroyAll()
  texture.destroyAll()
  shader.destroyAll()
  glfw.destroy()

}



















// Warning: gameLoop really should not be touched. Focus on the infrastructure around it before adding to it.
tailrec fun gameLoop() {
  window.update()

  update(getDelta())

  draw()

  window.swapBuffers()

  if (window.shouldClose()) return

  return gameLoop()
}

val atomicCounter = AtomicInteger(0)

//note: main is at the bottom because procedures should be put into the designated functions.
// Try not to modify this. It's the literal base of the entire program.
fun main(args: Array<String>) = runBlocking {
  println(args)

  load()

  Random.newSeed()



  blah()



  gameLoop()

  quit()

}

private val blah = ConcurrentLinkedDeque<Int>()

@OptIn(DelicateCoroutinesApi::class)
suspend fun blah() {
  coroutineScope {
    repeat(2) { threadID ->
      println("threadID: $threadID")
      GlobalScope.launch {
        if (threadID == 5) {
          println("all done.")
        } else {
          blah.add((random() * 100).toInt())
        }
      }
    }
  }
}