package engine

import kotlinx.coroutines.Runnable
import java.util.concurrent.Executors

// All functions which launch coroutines go into here.

object thread {
  // -1 because main thread.
  private const val FORWARD_CACHE = 24
  private val availableCores = Runtime.getRuntime().availableProcessors() + FORWARD_CACHE
  // Cached is better for servers.

  private val executor = Executors.newFixedThreadPool(availableCores)
  // note: runs out of control
//  private val executor = Executors.newCachedThreadPool()

//  fun formatSize(v: Long): String {
//    if (v < 1024) return "$v B"
//    val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
//    return String.format("%.1f %sB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
//  }
  internal fun launchAllThreads() {
//    val runtime = Runtime.getRuntime()
//    val heapSize = runtime.totalMemory()
//    val maxHeap = runtime.maxMemory()
//    val free = runtime.freeMemory()
//    println("heap: ${formatSize(heapSize)} | max: ${formatSize(maxHeap)} | free: ${formatSize(free)}")
    world.disperseChunkGenerators()
  }
  internal fun launch(work: Runnable) {
//    println("active threads: ${Thread.activeCount()}")
    executor.execute(work)
  }
  fun getCPUCores() = availableCores

  fun IntRange.parallelForEach(work: (Int) -> Unit) {
    if (this.step != 1) throw RuntimeException("Does not work for skip ranges.")
    val size = this.last - this.first
    val rangeSize = if (size / availableCores == 0) availableCores else size / availableCores
    val remainder = size % availableCores
    (0 until availableCores).forEach { i ->
      val start = this.first + (i * rangeSize)
      if (start > this.last) return
      val end = if (i == availableCores - 1) (start + rangeSize + remainder) else ((start + rangeSize) - 1)
      launch { (start..end).forEach(work) }
    }
  }

  internal fun destroy() {
    executor.shutdownNow()
  }
}