package org.jh.forum.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.di.AppModule

class MessageViewModel : ViewModel() {
    private val repository: ForumRepository = AppModule.forumRepository
    private val authViewModel: AuthViewModel = AppModule.authViewModel
    
    private val _hasUnreadMessages = MutableStateFlow(false)
    val hasUnreadMessages: StateFlow<Boolean> = _hasUnreadMessages.asStateFlow()
    
    private val _unreadNoticeCount = MutableStateFlow(0)
    val unreadNoticeCount: StateFlow<Int> = _unreadNoticeCount.asStateFlow()
    
    private val _unreadAnnouncementCount = MutableStateFlow(0)
    val unreadAnnouncementCount: StateFlow<Int> = _unreadAnnouncementCount.asStateFlow()
    
    init {
        // Start background task to check unread messages every minute
        viewModelScope.launch {
            while (true) {
                if (authViewModel.isLoggedIn.value) {
                    checkUnreadMessages()
                }
                delay(60000) // Wait for 1 minute
            }
        }
    }
    
    suspend fun checkUnreadMessages() {
        try {
            val response = repository.checkUnread()
            if (response.code == 200 && response.data != null) {
                updateUnreadCounts(response.data.unreadNoticeCount, response.data.unreadAnnouncementCount)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun updateUnreadCounts(noticeCount: Int, announcementCount: Int) {
        _unreadNoticeCount.value = noticeCount
        _unreadAnnouncementCount.value = announcementCount
        _hasUnreadMessages.value = noticeCount > 0 || announcementCount > 0
    }
}
