package engine.block

fun Int.toDrawType(): DrawType {
  return DrawType.entries.filter { it.data == this }
    .ifEmpty { throw RuntimeException("$this is not in range of drawtypes (0..8)") }[0]
}
