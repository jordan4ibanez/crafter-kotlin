package engine.block

import java.util.concurrent.ConcurrentHashMap

fun Int.toDrawType(): DrawType {
  return DrawType.entries.filter { it.data == this }
    .ifEmpty { throw RuntimeException("$this is not in range of drawtypes (0..8)") }[0]
}

//note: con stands for container.
internal fun <T> concurrent(): ConcurrentHashMap<Int, T> {
  return ConcurrentHashMap<Int, T>()
}

internal fun <T> singleThreaded(): HashMap<Int, T> {
  return HashMap()
}

// These two help with the BlockIDCache
internal const val cacheFolder = "./cache"
internal const val cacheFile = "./cache/block_cache.json"