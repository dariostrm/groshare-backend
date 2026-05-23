package shared.validation

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
private val USERNAME_REGEX = "^[a-zA-Z0-9._-]{3,30}$".toRegex()

fun String.ensureValidPassword() = ensure(length >= 8) { "Password must be at least 8 characters long" }

fun String.ensureValidEmail() = ensure(matches(EMAIL_REGEX)) { "Invalid email format" }

fun String.ensureValidUsername() {
    ensure(matches(USERNAME_REGEX)) {
        "Username must be between 3 and 30 characters and can only contain letters, numbers, dots, hyphens and underscores"
    }
}