package dev.jakobdario.shared

class UnauthorizedException(message: String = "Invalid credentials") : RuntimeException(message)
class ConflictException(message: String = "Conflict") : RuntimeException(message)
class ForbiddenException(message: String = "Forbidden") : RuntimeException(message)
class NotFoundException(message: String = "Not Found") : RuntimeException(message)