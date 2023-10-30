import engine.*
import engine.world.getBlockID
import kotlinx.coroutines.*
import org.joml.Math.*
import org.joml.Random
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import java.util.concurrent.ConcurrentLinkedDeque

// Initialization procedure. Consider this love.load()
var stone = 0
fun load() {

  println("Crafter is running on JVM ${System.getProperty("java.version")}")

  initialize()

  window.setVsync(false)

  // Debug texture.

  //todo: This is the basis for how world atlas is implemented into the game.

//  worldAtlas.add("1", "./textures/debug.png")
//  worldAtlas.add("blah", "./textures/text_box.png")
//  worldAtlas.add("fkalj", "./textures/test_thing.png")
//
//  texture.create("debug", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())

  texture.create("debug.png", "./textures/debug.png")

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
    "worldAtlas"
  )

//  mouse.capture()

//  window.maximize()
  stone = block.getID("crafter:stone")

  clientPlayer.initialize()
  clientPlayer.setPosition(clientPlayer.getPosition())
}




var color = 0f
var brighten = true
var speed = 0.5f
//var xOffset = 0f
//val noisey = Noise(123456)

// All general logic goes here. Consider this love.update()
fun update(delta: Float) {


//  noisey.setNoiseType(NoiseType.Simplex)
//  noisey.setFrequency(0.01f)

//  camera.freeCam()

  camera.doMouseInputCameraRotation()
  clientPlayer.doClientControls()

  if (keyboard.isDown(GLFW_KEY_ESCAPE)){
   window.close()
    return
  }

  if (keyboard.isPressed(GLFW_KEY_F1)) {
    mouse.toggleCapture()
  }

  if (fpsUpdated()) {
    window.setTitle("FPS: ${getFPS()}")
  }

  if (brighten) {
    color += delta * speed
    if (color >= 1f) {
      color = 1f
      brighten = false
    }
  } else {
    color -= delta * speed
    if (color <= 0f) {
      color = 0f
      brighten = true
    }
  }
  window.setClearColor(color)
}

// Consider this pure logic updates. onTick, collision, etc. For real time things, put them in update().
fun tick(delta: Float) {

  // Global tick.
  api.doOnTick(delta)
  // Entity specific tick.
  entity.doOnTick(delta)

}



// All draw procedures go here. Consider this love.draw()
var rotation = 0f
fun draw() {
//  rotation += getDelta() * 50f
//  println(rotation)
  camera.updateCameraMatrix()
  camera.setObjectMatrix(Vector3f(0f, 0f, -1f), Vector3f(0f,toRadians(rotation), 0f))
  mesh.draw("debug")

  world.renderChunks()

  entity.draw()

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

  val delta = getDelta()

  if (tick.think(delta)) {
    tick(delta)
    launchAllThreads()
  }

  update(delta)

  draw()

  window.swapBuffers()

  if (window.shouldClose()) return

  return gameLoop()
}


//note: main is at the bottom because procedures should be put into the designated functions.
// Try not to modify this. It's the literal base of the entire program.
fun main(args: Array<String>) = runBlocking {
  println(args)

  Random.newSeed()

  load()

  gameLoop()

  quit()

}

