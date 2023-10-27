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
}




var color = 0f
var brighten = true
var speed = 0.5f
var xOffset = 0f
val noisey = Noise(123456)

// All general logic goes here. Consider this love.update()
fun update(delta: Float) {
  noisey.setNoiseType(NoiseType.Simplex)
  noisey.setFrequency(0.01f)

  camera.freeCam()

  if (keyboard.isDown(GLFW_KEY_ESCAPE)){
   window.close()
    return
  }

  if (keyboard.isPressed(GLFW_KEY_F1)) {
    mouse.toggleCapture()
  }

  if (keyboard.isPressed(GLFW_KEY_R)) {
    println("BLEH")
//    regenerateWorldAtlas()
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

  if (entity.hasPlayer("singleplayer")) {
//    println(1)
    val pos = entity.getPlayer("singleplayer").getPosition()
      val (x,y,z) = pos.destructure()
      if (blockManipulator.set(
          x - 32, 0f, z - 32,
          x + 31, 127f, z + 31)) {
        //? note: ocean version

        val waterID = block.getID("crafter:water")
        val airID = block.getID("air")
        val waveSpeed = 10.0f
        xOffset += delta * waveSpeed

        val index = 0
        for (x in blockManipulator.getMin)

        //?note: groovy version
//        val grassID = block.getID("crafter:grass")
//        val airID = block.getID("air")
//        val waveSpeed = 10.0f
//        xOffset += delta * waveSpeed
//        var index = 0
//        blockManipulator.forEach { data ->
//          val (bx,by,bz) = blockManipulator.indexToPos(index).destructure()
//          if (blockManipulator.inBounds(bx,by + 1,bz)) {
//            if (data.getBlockID() != airID && blockManipulator.getID(bx,by + 1,bz) == airID) {
//
//              val newID = clamp(1, 8, ((noisey.getSimplex(bx.toFloat() + xOffset, 0f, bz.toFloat()) + 1f) * 9f).toInt())
//
//                blockManipulator.setID(index, newID)
//              }
//          }
//          index++
//        }
//        blockManipulator.write()
      }

//      println(2)
//      block.getName(world.getBlockID(pos)).apply(::println)
//      (0..3).forEach {
//        world.setBlockID(x,y+it,z, (random() * 8f).toInt())

//    }
  }

//  println(color)


}

//fun regenerateWorldAtlas() {
//
//  worldAtlas.clear()
//  worldAtlas.add("1", "./textures/debug.png")
//  worldAtlas.add("blah", "./textures/text_box.png")
//  worldAtlas.add("fkalj", "./textures/test_thing.png")
//
//  texture.destroy("debug")
//  texture.create("debug", worldAtlas.flush(), worldAtlas.getSize(), worldAtlas.getChannels())
//  mesh.swapTexture("debug", "debug")
//}



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
tailrec suspend fun gameLoop() {

  window.update()

  launchAllThreads()

  update(getDelta())

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