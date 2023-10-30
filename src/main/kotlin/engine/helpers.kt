package engine

// Thanks, kimapr! This will be quite useful. https://stackoverflow.com/a/34608552
inline fun <T> Array<T>.mapInPlace(mutator: (T) -> T) {
  this.forEachIndexed { idx, value ->
    mutator(value).let { newValue ->
      if (newValue !== value) this[idx] = mutator(value)
    }
  }
}

inline fun <T> Array<T>.mapInPlaceIndexed(mutator: (Int, T) -> T) {
  this.forEachIndexed { idx, value ->
    mutator(idx, value).let { newValue ->
      if (newValue !== value) this[idx] = mutator(idx, value)
    }
  }
}

inline fun IntArray.mapInPlace(mutator: (Int) -> Int) {
  this.forEachIndexed { idx, value ->
    mutator(value).let { newValue ->
      if (newValue != value) this[idx] = mutator(value)
    }
  }
}

inline fun IntArray.mapInPlaceIndexed(mutator: (Int, Int) -> Int) {
  this.forEachIndexed { idx, value ->
    mutator(idx, value).let { newValue ->
      if (newValue != value) this[idx] = mutator(idx, value)
    }
  }
}

inline fun FloatArray.mapInPlace(mutator: (Float) -> Float) {
  this.forEachIndexed { idx, value ->
    mutator(value).let { newValue ->
      if (newValue != value) this[idx] = mutator(value)
    }
  }
}

inline fun FloatArray.mapInPlaceIndexed(mutator: (Int, Float) -> Float) {
  this.forEachIndexed { idx, value ->
    mutator(idx, value).let { newValue ->
      if (newValue != value) this[idx] = mutator(idx, value)
    }
  }
}

infix fun Int.toward(to: Int): IntProgression {
  val step = if (this > to) -1 else 1
  return IntProgression.fromClosedRange(this, to, step)
}