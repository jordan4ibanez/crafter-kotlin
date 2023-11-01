package engine

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

// All functions which launch coroutines go into here.

object thread {
  // -1 because main thread.
  private val availableCores = Runtime.getRuntime().availableProcessors() - 1
  private val executor = Executors.newFixedThreadPool(availableCores)

  internal fun launchAllThreads() {
    world.disperseChunkGenerators()
  }
  internal fun launch(work: Runnable) {
    executor.execute(work)
  }
  fun getCPUCores() = availableCores

  val debugger = (0..100).parallelForEach { print(it) }

  fun IntRange.parallelForEach(work: (Int) -> Unit) {
    this.forEach { work(it) }
  }
}