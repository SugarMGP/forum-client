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
import org.jh.forum.client.data.model.GetAnnouncementListElement
import org.jh.forum.client.data.model.GetNoticeListElement
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

    // Messages and announcements data
    private val _messages = MutableStateFlow<List<GetNoticeListElement>>(emptyList())
    val messages: StateFlow<List<GetNoticeListElement>> = _messages.asStateFlow()

    private val _announcements = MutableStateFlow<List<GetAnnouncementListElement>>(emptyList())
    val announcements: StateFlow<List<GetAnnouncementListElement>> = _announcements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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

    // Selected types
    private val _selectedNoticeType = MutableStateFlow(0)
    val selectedNoticeType: StateFlow<Int> = _selectedNoticeType.asStateFlow()

    private val _selectedType = MutableStateFlow(0) // 0: 互动, 1: 公告
    val selectedType: StateFlow<Int> = _selectedType.asStateFlow()

    private val _selectedAnnouncementType = MutableStateFlow(0)
    val selectedAnnouncementType: StateFlow<Int> = _selectedAnnouncementType.asStateFlow()

    init {
        viewModelScope.launch {
            // 当登录状态变化时触发
            authViewModel.isLoggedIn.collectLatest { loggedIn ->
                if (loggedIn) {
                    // 立刻检查一次
                    checkUnreadMessages()
                    // Load initial messages
                    loadNotices(reset = true)
                    while (isActive && authViewModel.isLoggedIn.value) {
                        delay(60_000)
                        checkUnreadMessages()
                    }
                } else {
                    // 登出时清零
                    updateUnreadCounts(0, 0)
                    _messages.value = emptyList()
                    _announcements.value = emptyList()
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

    fun loadNotices(reset: Boolean = false) {
        viewModelScope.launch {
            if (!reset && !_noticeHasMore.value) return@launch

            _isLoading.value = true
            _errorMessage.value = null
            try {
                val page = if (reset) 1 else _noticeCurrentPage.value
                val noticeResponse = repository.getNoticeList(
                    page = page,
                    pageSize = 20,
                    type = _selectedNoticeType.value
                )
                if (noticeResponse.code == 200 && noticeResponse.data != null) {
                    val response = noticeResponse.data
                    val newNotices = response.list

                    _messages.value = if (reset) {
                        newNotices
                    } else {
                        val existingIds = _messages.value.map { it.id }.toSet()
                        val uniqueNewNotices = newNotices.filter { it.id !in existingIds }
                        _messages.value + uniqueNewNotices
                    }

                    val hasMore = page * response.pageSize < response.total
                    _noticeCurrentPage.value = if (hasMore) page + 1 else page
                    _noticeHasMore.value = hasMore
                } else {
                    _errorMessage.value = "加载通知失败"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "加载通知失败"
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun loadAnnouncements(reset: Boolean = false) {
        viewModelScope.launch {
            if (!reset && !_announcementHasMore.value) return@launch

            _isLoading.value = true
            _errorMessage.value = null
            try {
                val page = if (reset) 1 else _announcementCurrentPage.value
                val type = when (_selectedAnnouncementType.value) {
                    1 -> "scholastic"
                    2 -> "systematic"
                    else -> ""
                }
                val announcementResponse = repository.getAnnouncementList(
                    page = page,
                    pageSize = 20,
                    type = type
                )
                if (announcementResponse.code == 200 && announcementResponse.data != null) {
                    val response = announcementResponse.data
                    val newAnnouncements = response.list

                    _announcements.value = if (reset) {
                        newAnnouncements
                    } else {
                        val existingIds = _announcements.value.map { it.id }.toSet()
                        val uniqueNewAnnouncements = newAnnouncements.filter { it.id !in existingIds }
                        _announcements.value + uniqueNewAnnouncements
                    }

                    val hasMore = page * response.pageSize < response.total
                    _announcementCurrentPage.value = if (hasMore) page + 1 else page
                    _announcementHasMore.value = hasMore
                } else {
                    _errorMessage.value = "加载公告失败"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "加载公告失败"
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun setSelectedNoticeType(type: Int) {
        if (_selectedNoticeType.value != type) {
            _selectedNoticeType.value = type
            resetNoticePagination()
            loadNotices(reset = true)
        }
    }

    fun setSelectedType(type: Int) {
        if (_selectedType.value != type) {
            _selectedType.value = type
            if (type == 0) {
                loadNotices(reset = true)
            } else {
                loadAnnouncements(reset = true)
            }
        }
    }

    fun setSelectedAnnouncementType(type: Int) {
        if (_selectedAnnouncementType.value != type) {
            _selectedAnnouncementType.value = type
            resetAnnouncementPagination()
            loadAnnouncements(reset = true)
        }
    }

    fun retry() {
        if (_selectedType.value == 0) {
            resetNoticePagination()
            loadNotices(reset = true)
        } else {
            resetAnnouncementPagination()
            loadAnnouncements(reset = true)
        }
    }

    private fun resetNoticePagination() {
        _noticeCurrentPage.value = 1
        _noticeHasMore.value = true
    }

    private fun resetAnnouncementPagination() {
        _announcementCurrentPage.value = 1
        _announcementHasMore.value = true
    }
}
