package utility.safety_exceptions

/**
 * Exception class which indicates that nothing was unwrapped.
 */
class UnwrapException(info: String) : Exception(info)

/**
 * Exception class which indicates that something was expected, but contained nothing.
 */
class ExpectException(info: String) : Exception(info)