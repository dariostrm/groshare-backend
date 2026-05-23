package auth

import at.favre.lib.crypto.bcrypt.BCrypt

@JvmInline
value class PasswordHash(val value: String)

object PasswordManager {
    fun hashPassword(password: String): PasswordHash =
         PasswordHash(BCrypt.withDefaults().hashToString(12, password.toCharArray()))

    fun verify(password: String, hash: PasswordHash): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash.value).verified
}