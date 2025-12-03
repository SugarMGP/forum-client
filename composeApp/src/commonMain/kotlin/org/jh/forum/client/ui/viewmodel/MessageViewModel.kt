package org.jh.forum.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
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

    // Pagination state for notices
    private val _noticeCurrentPage = MutableStateFlow(1)
    val noticeCurrentPage: StateFlow<Int> = _noticeCurrentPage.asStateFlow()

    private val _noticeHasMore = MutableStateFlow(true)
    val noticeHasMore: StateFlow<Boolean> = _noticeHasMore.asStateFlow()

    // Pagination state for announcements
    private val _announcementCurrentPage = MutableStateFlow(1)
    val announcementCurrentPage: StateFlow<Int> = _announcementCurrentPage.asStateFlow()

    private val _announcementHasMore = MutableStateFlow(true)
    val announcementHasMore: StateFlow<Boolean> = _announcementHasMore.asStateFlow()

    init {
        viewModelScope.launch {
            // 当登录状态变化时触发
            authViewModel.isLoggedIn.collectLatest { loggedIn ->
                if (loggedIn) {
                    // 立刻检查一次
                    checkUnreadMessages()
                    while (isActive && authViewModel.isLoggedIn.value) {
                        delay(60_000)
                        checkUnreadMessages()
                    }
                } else {
                    // 登出时清零
                    updateUnreadCounts(0, 0)
                }
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

    fun cleanUnreadBadge() {
        updateUnreadCounts(0, 0)
    }

    fun updateUnreadCounts(noticeCount: Int, announcementCount: Int) {
        _unreadNoticeCount.value = noticeCount
        _unreadAnnouncementCount.value = announcementCount
        _hasUnreadMessages.value = noticeCount > 0 || announcementCount > 0
    }

    fun updateNoticePagination(currentPage: Int, hasMore: Boolean) {
        _noticeCurrentPage.value = currentPage
        _noticeHasMore.value = hasMore
    }

    fun updateAnnouncementPagination(currentPage: Int, hasMore: Boolean) {
        _announcementCurrentPage.value = currentPage
        _announcementHasMore.value = hasMore
    }

    fun resetNoticePagination() {
        _noticeCurrentPage.value = 1
        _noticeHasMore.value = true
    }

    fun resetAnnouncementPagination() {
        _announcementCurrentPage.value = 1
        _announcementHasMore.value = true
    }
}
