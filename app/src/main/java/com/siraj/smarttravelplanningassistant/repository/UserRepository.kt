package com.siraj.smarttravelplanningassistant.repository

import com.siraj.smarttravelplanningassistant.database.User
import com.siraj.smarttravelplanningassistant.database.UserDao

class UserRepository(private val userDao: UserDao) {
    suspend fun registerUser(user: User): Boolean {
        return try {
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginUser(email: String, password: String): User? {
        return userDao.getUser(email, password)
    }
}
