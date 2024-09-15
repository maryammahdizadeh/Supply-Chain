package com.example.supplychainapp

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class UserRepository(private val userDao: UserDao) {


    // Expose the list of users as a Flow
    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    // Function to insert users
    suspend fun insertUsers(users: List<User>) {
        userDao.insertUsers(users)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    // Function to delete all users
    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }


    suspend fun loginUser(username: String, password: String): User? {
        return userDao.getUser(username, password)
    }

    suspend fun getUserId(username: String, password: String): Int? {
        return userDao.getUserId(username, password)
    }

    suspend fun resetUserIdSequence() {
        userDao.resetUserIdSequence()
    }
}