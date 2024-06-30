import engine.api.Api
import engine.block.Block
import engine.camera.Camera
import engine.client_player.ClientPlayer
import engine.delta_time.DeltaTime
import engine.entity.EntityHandler
import engine.glfw.Glfw
import engine.initialization.initialize
import engine.keyboard.Keyboard
import engine.model.mesh.Mesh
import engine.model.texture.Texture
import engine.mouse.Mouse
import engine.shader.Shader
import engine.thread.Thread
import engine.tick.Tick
import engine.window.Window
import engine.world.world
import org.joml.Math.toRadians
import org.joml.Random
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_KEY_F1

// Initialization procedure. Consider this love.load()
var stone = 0
fun load() {

  println("Crafter is running on JVM ${System.getProperty("java.version")}")

  initialize()

  Window.setVsync(false)

  // Debug texture.

  //todo: This is the basis for how world atlas is implemented into the game.

//  worldAtlas.add("1", "./textures/debug.png")
//  worldAtlas.add("blah", "./textures/text_box.png")
//  worldAtlas.add("fkalj", "./textures/test_thing.png")
//
//  texture.create("debug", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())

  Texture.create("debug.png", "./textures/debug.png")

  // Debug mesh.
  Mesh.create3D(
    "debug",
    // positions.
    floatArrayOf(
      -0.5f, 0.5f, 0f,
      -0.5f, -0.5f, 0f,
      0.5f, -0.5f, 0f,
      0.5f, 0.5f, 0f
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
  Window.setVsync(true)
  stone = Block.getID("crafter:stone")

  ClientPlayer.initialize()
  ClientPlayer.setPosition(ClientPlayer.getPosition())
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

  Camera.doMouseInputCameraRotation()
  ClientPlayer.doClientControls()
  EntityHandler.doOnStep(delta)
  ClientPlayer.glueCamera()


  if (Keyboard.isDown(GLFW_KEY_ESCAPE)) {
    Window.close()
    return
  }

  if (Keyboard.isPressed(GLFW_KEY_F1)) {
    Mouse.toggleCapture()
  }

  if (DeltaTime.fpsUpdated()) {
    Window.setTitle("FPS: ${DeltaTime.getFPS()}")
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
  Window.setClearColor(color)
}

// Consider this pure logic updates. onTick, collision, etc. For real time things, put them in update().
fun tick(delta: Float) {

  // Global tick.
  Api.doOnTick(delta)
  // Entity specific tick.
  EntityHandler.doOnTick(delta)
}

// All draw procedures go here. Consider this love.draw()
var rotation = 0f
fun draw() {
//  rotation += getDelta() * 50f
//  println(rotation)
  Camera.updateCameraMatrix()
  Camera.setObjectMatrix(Vector3f(0f, 0f, -1f), Vector3f(0f, toRadians(rotation), 0f))
  Mesh.draw("debug")

  world.renderChunks()

  EntityHandler.draw()
}

// Game cleanup procedures go here. Consider this love.quit()
fun quit() {

  Thread.destroy()
  Mesh.destroyAll()
  Texture.destroyAll()
  Shader.destroyAll()
  Glfw.destroy()
}

// Warning: gameLoop really should not be touched. Focus on the infrastructure around it before adding to it.
tailrec fun gameLoop() {

  Window.update()

  val delta = DeltaTime.getDelta()

  if (Tick.think(delta)) {
    tick(delta)
    Thread.launchAllThreads()
  }

  update(delta)

  draw()

  Window.swapBuffers()

  if (Window.shouldClose()) return

  return gameLoop()
}

//note: main is at the bottom because procedures should be put into the designated functions.
// Try not to modify this. It's the literal base of the entire program.
fun main(args: Array<String>) {
  println(args)

  Random.newSeed()

  load()

  gameLoop()

  quit()
}

