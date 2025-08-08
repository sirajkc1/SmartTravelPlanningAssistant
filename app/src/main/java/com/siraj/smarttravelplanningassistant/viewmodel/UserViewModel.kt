package com.siraj.smarttravelplanningassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.smarttravelplanningassistant.database.User
import com.siraj.smarttravelplanningassistant.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _signupSuccess = MutableStateFlow(false)
    val signupSuccess: StateFlow<Boolean> = _signupSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentUserEmail: String? = null

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = repository.loginUser(email, password)
            if (user != null) {
                currentUserEmail = user.email
                _loginSuccess.value = true
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Invalid credentials"
                _loginSuccess.value = false
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            val result = repository.registerUser(user)
            if (result) {
                // Registration succeeded
                _errorMessage.value = null
                _signupSuccess.value = true
            } else {
                // Registration failed (e.g., email already used)
                _errorMessage.value = "Registration failed (email already used)"
                _signupSuccess.value = false
            }
        }
    }

    fun logout() {
        currentUserEmail = null
        _loginSuccess.value = false
    }

    fun getCurrentUserEmail(): String? = currentUserEmail

    fun resetSignupSuccess() {
        _signupSuccess.value = false
    }
}
