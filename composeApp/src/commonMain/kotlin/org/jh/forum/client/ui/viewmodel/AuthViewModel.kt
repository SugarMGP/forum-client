package org.jh.forum.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.GetUserProfileResponse
import org.jh.forum.client.data.model.LoginRequest
import org.jh.forum.client.data.model.UpdateUserProfileRequest
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.di.AppModule

class AuthViewModel : ViewModel() {
    private val repository: ForumRepository = AppModule.forumRepository
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userProfile = MutableStateFlow<GetUserProfileResponse?>(null)
    val userProfile: StateFlow<GetUserProfileResponse?> = _userProfile.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val request = LoginRequest(username = username, password = password)
            val result = repository.login(request)
            if (result.code == 200 && result.data != null) {
                _isLoggedIn.value = true
                // 登录成功后获取用户信息
                getProfile()
            } else {
                _errorMessage.value = result.msg ?: "登录失败"
            }
            _isLoading.value = false
        }
    }

    // 检查登录状态
    fun checkAuthStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getProfile()
            if (result.code == 200 && result.data != null) {
                _isLoggedIn.value = true
                _userProfile.value = result.data
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.logout()
            if (result.code == 200) {
                _isLoggedIn.value = false
                _userProfile.value = null
            }
            _isLoading.value = false
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getProfile()
            if (result.code == 200 && result.data != null) {
                _userProfile.value = result.data
            } else {
                _errorMessage.value = result.msg ?: "获取用户信息失败"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(request: UpdateUserProfileRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateProfile(request)
            if (result.code == 200) {
                getProfile() // 更新成功后重新获取用户信息
            } else {
                _errorMessage.value = result.msg ?: "更新用户信息失败"
            }
            _isLoading.value = false
        }
    }
}