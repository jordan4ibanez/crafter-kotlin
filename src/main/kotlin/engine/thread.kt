package engine

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

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

  val debugger = (-500 until 500).parallelForEach { println(it) }

  fun IntRange.parallelForEach(work: (Int) -> Unit) {
    if (this.step != 1) throw RuntimeException("Does not work for skip ranges.")
    val size = max(this.first, this.last) - min(this.first, this.last)
//    println("size $size")
    val rangeSize = size / availableCores
    val remainder = size % availableCores
//    println("total: stepsIn $rangeSize, lastStep $remainder = ${(rangeSize * availableCores) + remainder}")
//    this.forEach { work(it) }
//    println("remainder: $remainder")
    (0 until availableCores).forEach { i ->
      val start = this.first + (i * rangeSize)
      val end =  if (i == (availableCores - 1))  (start + rangeSize) + remainder else (start + rangeSize) - 1
      launch {
        (start .. end).forEach(work)
      }
//      println("start: $start | end: $end")
    }
  }
}