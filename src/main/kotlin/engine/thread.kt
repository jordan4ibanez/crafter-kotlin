package engine

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

// All functions which launch coroutines go into here.

object thread {
  // -1 because main thread.
  private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
  internal fun launchAllThreads() {
    world.disperseChunkGenerators()
  }
  fun launch(work: Runnable) {
    executor.execute(work)
  }
}