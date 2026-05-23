package shared

class UnauthorizedException(message: String = "Invalid credentials") : RuntimeException(message)