package engine

// Micro ternary statement
fun <T>tern(condition: Boolean, trueState: T, falseState: T): T {
  return if (condition) trueState else falseState
}


