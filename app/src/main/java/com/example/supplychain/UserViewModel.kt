package com.example.supplychainapp

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = DatabaseInstance.getDatabase(application).userDao()
    private val repository: UserRepository = UserRepository(userDao)

    // Use StateFlow to expose the list of users to the UI
    val allUsers: StateFlow<List<User>> = repository.allUsers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )


    // Insert users into the database
    fun insertUsers(users: List<User>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUsers(users)
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUser(user)
        }
    }

    // Delete all users from the database
    fun deleteAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUsers()
        }
    }

    fun resetUserIdSequence() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetUserIdSequence()
        }
    }


//    fun getUserId(username: String, password: String): LiveData<Int?> {
//        val userId = MutableLiveData<Int?>()
//        viewModelScope.launch {
//            userId.value = repository.getUserId(username, password)
//        }
//        return userId
//    }

//    private val _userId = mutableStateOf<Int?>(null)
//    val userId: State<Int?> = _userId
//
//    fun fetchUserId(username: String, password: String) {
//        viewModelScope.launch {
//            val id = repository.getUserId(username, password)
//            _userId.value = id
//        }
//    }

//    fun getUserId(username: String, password: String, onResult: (Int) -> Unit) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val user = repository.loginUser(username, password)
//                withContext(Dispatchers.Main) {
//                    if (user != null) {
//                        // Assuming `user.id` is the user's ID
//                        onResult(user.id) // Return the user's ID on successful login
//                    } else {
//                        onResult(-1) // Return -1 to indicate login failure
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("UserViewModel", "Error logging in", e)
//                withContext(Dispatchers.Main) {
//                    onResult(-2) // Return -2 to indicate an error
//                }
//            }
//        }
//    }




    // Function to check if the user exists
//    fun loginUser(username: String, password: String, onResult: (Boolean) -> Unit) {
////        viewModelScope.launch(Dispatchers.IO) {
////            val user = repository.loginUser(username, password)
////            // On success, the user is found, otherwise null
////            onResult(user != null)
////        }
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val user = repository.loginUser(username, password)
//                withContext(Dispatchers.Main) {
//                    // Switch to the main thread to perform UI actions
//                    onResult(user != null)
//                }
//            } catch (e: Exception) {
//                Log.e("UserViewModel", "Error logging in", e)
//                withContext(Dispatchers.Main) {
//                    onResult(false)
//                }
//            }
//        }
//    }

    fun loginUser(username: String, password: String, onResult: (Int?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.loginUser(username, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // If login is successful, pass the userId
                        onResult(user.id)
                    } else {
                        // If login fails, pass null
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error logging in", e)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }
}