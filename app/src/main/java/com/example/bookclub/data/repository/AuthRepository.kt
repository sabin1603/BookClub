package com.example.bookclub.data.repository

import com.example.bookclub.data.local.dao.UserDao
import com.example.bookclub.data.local.entity.UserEntity
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao
) {

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<UserEntity> {
        val cleanUsername = username.trim()
        val cleanEmail = email.trim()

        if (cleanUsername.length < 3) {
            return Result.failure(Exception("Username must have at least 3 characters."))
        }

        if (!cleanEmail.contains("@")) {
            return Result.failure(Exception("Enter a valid email address."))
        }

        if (password.length < 4) {
            return Result.failure(Exception("Password must have at least 4 characters."))
        }

        val alreadyExists = userDao.countByEmailOrUsername(cleanEmail, cleanUsername) > 0
        if (alreadyExists) {
            return Result.failure(Exception("Username or email already exists."))
        }

        return try {
            val user = UserEntity(
                username = cleanUsername,
                email = cleanEmail,
                passwordHash = hashPassword(password)
            )

            val id = userDao.insertUser(user)
            Result.success(user.copy(id = id))
        } catch (e: Exception) {
            Result.failure(Exception("Could not create account."))
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<UserEntity> {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password are required."))
        }

        val user = userDao.findByEmail(cleanEmail)
            ?: return Result.failure(Exception("Account not found."))

        return if (user.passwordHash == hashPassword(password)) {
            Result.success(user)
        } else {
            Result.failure(Exception("Incorrect password."))
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") { "%02x".format(it) }
    }
}