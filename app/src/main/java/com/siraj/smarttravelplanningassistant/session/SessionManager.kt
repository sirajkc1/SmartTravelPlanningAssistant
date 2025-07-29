package com.siraj.smarttravelplanningassistant.session

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager : ViewModel() {
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    fun login(email: String) {
        _userEmail.value = email
    }

    fun logout() {
        _userEmail.value = null
    }
}
