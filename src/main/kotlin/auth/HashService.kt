package dev.jakobdario.auth

import at.favre.lib.crypto.bcrypt.BCrypt

data class Hash(val value: String)

interface HashService {
    fun hashPassword(password: String): Hash
    fun verify(password: String, hash: Hash): Boolean
}

class HashServiceImpl() : HashService {
    override fun hashPassword(password: String): Hash =
         Hash(BCrypt.withDefaults().hashToString(12, password.toCharArray()))

    override fun verify(password: String, hash: Hash): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash.value).verified
}