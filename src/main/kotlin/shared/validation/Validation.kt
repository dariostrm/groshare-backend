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

fun String.ensureValidApartmentName() = ensure(length in 2..50) { "Name must be between 2 and 50 characters" }

fun String.ensureValidAddress() = ensure(length in 5..100) { "Address must be between 5 and 100 characters" }

fun String.ensureValidCity() = ensure(length in 2..50) { "City must be between 2 and 50 characters" }