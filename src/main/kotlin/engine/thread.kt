package engine

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

// All functions which launch coroutines go into here.

object thread {
  // -1 because main thread.
  private val availableCores = Runtime.getRuntime().availableProcessors() - 1
  // Cached is better for servers.
  private val executor = Executors.newCachedThreadPool()//Executors.newFixedThreadPool(availableCores)

  internal fun launchAllThreads() {
    world.disperseChunkGenerators()
  }
  internal fun launch(work: Runnable) {
//    println(Thread.activeCount())
    executor.execute(work)
  }
  fun getCPUCores() = availableCores

  fun IntRange.parallelForEach(work: (Int) -> Unit) {
    if (this.step != 1) throw RuntimeException("Does not work for skip ranges.")
    val size = max(this.first, this.last) - min(this.first, this.last)
    val rangeSize = size / availableCores
    val remainder = size % availableCores
    (0 until availableCores).forEach { i ->
      val start = this.first + (i * rangeSize)
      val end =  if (i == (availableCores - 1))  (start + rangeSize) + remainder else (start + rangeSize) - 1
      launch {
        (start .. end).forEach(work)
      }
    }
  }

  fun <K, V> ConcurrentHashMap<K, V>.parallelForEach(work: (Int) -> Unit) {
    if (this.step != 1) throw RuntimeException("Does not work for skip ranges.")
  }

   fun <K, V> Map<out K, V>.forEach(work: (Map.Entry<K, V>) -> Unit) {
     val size = this.size
     val rangeSize = size / availableCores
     val remainder = size % availableCores

     (0 until availableCores).forEach { i ->
       this.keys.
     }
  }
}