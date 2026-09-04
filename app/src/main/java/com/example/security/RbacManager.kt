package com.example.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class Permission {
    RECORD_INTAKE,
    RECORD_DISPATCH,
    APPROVE_OVERRIDE,
    MANAGE_PAYMENTS,
    CLEAR_PDC,
    CLOSE_DAY_END,
    VIEW_FINANCIAL_PNL,
    EXPORT_REPORTS,
    MANAGE_USERS,
    DELETE_TRANSACTION
}

/**
 * Enterprise RBAC security and PBKDF2 Salted PIN hashing manager.
 */
object RbacManager {

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String = ""): String {
        return if (salt.isBlank()) {
            // Legacy / unsalted fallback
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } else {
            val saltBytes = salt.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val spec = PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            hash.joinToString("") { "%02x".format(it) }
        }
    }

    fun verifyPin(enteredPin: String, storedHash: String, salt: String = ""): Boolean {
        return hashPin(enteredPin, salt) == storedHash || (salt.isNotBlank() && hashPin(enteredPin, "") == storedHash)
    }

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return when (role) {
            UserRole.OWNER -> true // Owner has omnipotent access
            UserRole.OPERATOR -> when (permission) {
                Permission.RECORD_INTAKE,
                Permission.RECORD_DISPATCH -> true
                else -> false
            }
            UserRole.ACCOUNTANT -> when (permission) {
                Permission.MANAGE_PAYMENTS,
                Permission.CLEAR_PDC,
                Permission.CLOSE_DAY_END,
                Permission.VIEW_FINANCIAL_PNL,
                Permission.EXPORT_REPORTS -> true
                else -> false
            }
            UserRole.VIEWER -> when (permission) {
                Permission.VIEW_FINANCIAL_PNL,
                Permission.EXPORT_REPORTS -> true
                else -> false
            }
        }
    }
}
