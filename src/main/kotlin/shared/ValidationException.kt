package shared

class ValidationException(message: String) : RuntimeException(message)

/**
 * Throws a [ValidationException] with the result of calling [lazyMessage] if the [value] is false.
 */
inline fun ensure(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw ValidationException(message.toString())
    }
}