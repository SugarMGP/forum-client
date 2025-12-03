package org.jh.forum.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.GetPersonalPostListElement
import org.jh.forum.client.data.model.PersonalCommentListElement
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.di.AppModule

class UserProfileViewModel : ViewModel() {
    private val repository: ForumRepository = AppModule.forumRepository

    // Posts state
    private val _posts = MutableStateFlow<List<GetPersonalPostListElement>>(emptyList())
    val posts: StateFlow<List<GetPersonalPostListElement>> = _posts.asStateFlow()

    private val _postsLoading = MutableStateFlow(false)
    val postsLoading: StateFlow<Boolean> = _postsLoading.asStateFlow()

    private val _postsHasMore = MutableStateFlow(true)
    val postsHasMore: StateFlow<Boolean> = _postsHasMore.asStateFlow()

    private val _postsCurrentPage = MutableStateFlow(1)
    val postsCurrentPage: StateFlow<Int> = _postsCurrentPage.asStateFlow()

    // Comments state
    private val _comments = MutableStateFlow<List<PersonalCommentListElement>>(emptyList())
    val comments: StateFlow<List<PersonalCommentListElement>> = _comments.asStateFlow()

    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()

    private val _commentsHasMore = MutableStateFlow(true)
    val commentsHasMore: StateFlow<Boolean> = _commentsHasMore.asStateFlow()

    private val _commentsCurrentPage = MutableStateFlow(1)
    val commentsCurrentPage: StateFlow<Int> = _commentsCurrentPage.asStateFlow()

    // Current user ID being viewed
    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    fun setUserId(userId: Long) {
        if (_currentUserId.value != userId) {
            _currentUserId.value = userId
            resetPostsState()
            resetCommentsState()
        }
    }

    fun loadPosts(reset: Boolean = false) {
        viewModelScope.launch {
            if (!reset && !_postsHasMore.value) return@launch

            val userId = _currentUserId.value ?: return@launch

            _postsLoading.value = true
            try {
                val page = if (reset) 1 else _postsCurrentPage.value
                val result = repository.getPersonalPostList(
                    page = page,
                    pageSize = 20,
                    userId = userId
                )

                if (result.code == 200 && result.data != null) {
                    val postList = result.data
                    _posts.value = if (reset) {
                        postList.list
                    } else {
                        // Merge new posts with existing ones, filtering out duplicates
                        val existingIds = _posts.value.map { it.id }.toSet()
                        val uniqueNewPosts = postList.list.filter { it.id !in existingIds }
                        _posts.value + uniqueNewPosts
                    }

                    _postsHasMore.value = postList.page * postList.pageSize < postList.total
                    if (_postsHasMore.value && !reset) {
                        _postsCurrentPage.value = page + 1
                    } else if (reset) {
                        _postsCurrentPage.value = 2
                    }
                }
                _postsLoading.value = false
            } catch (e: Exception) {
                _postsLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun loadComments(reset: Boolean = false) {
        viewModelScope.launch {
            if (!reset && !_commentsHasMore.value) return@launch

            _commentsLoading.value = true
            try {
                val page = if (reset) 1 else _commentsCurrentPage.value
                val result = repository.getPersonalComment(
                    page = page,
                    pageSize = 20
                )

                if (result.code == 200 && result.data != null) {
                    val commentList = result.data
                    _comments.value = if (reset) {
                        commentList.list
                    } else {
                        // Merge new comments with existing ones, filtering out duplicates
                        val existingKeys = _comments.value.map {
                            if (it.replyId != 0L) "reply_${it.replyId}" else "comment_${it.commentId}"
                        }.toSet()
                        val uniqueNewComments = commentList.list.filter { comment ->
                            val key =
                                if (comment.replyId != 0L) "reply_${comment.replyId}" else "comment_${comment.commentId}"
                            key !in existingKeys
                        }
                        _comments.value + uniqueNewComments
                    }

                    _commentsHasMore.value = commentList.page * commentList.pageSize < commentList.total
                    if (_commentsHasMore.value && !reset) {
                        _commentsCurrentPage.value = page + 1
                    } else if (reset) {
                        _commentsCurrentPage.value = 2
                    }
                }
                _commentsLoading.value = false
            } catch (e: Exception) {
                _commentsLoading.value = false
                e.printStackTrace()
            }
        }
    }

    private fun resetPostsState() {
        _posts.value = emptyList()
        _postsCurrentPage.value = 1
        _postsHasMore.value = true
        _postsLoading.value = false
    }

    private fun resetCommentsState() {
        _comments.value = emptyList()
        _commentsCurrentPage.value = 1
        _commentsHasMore.value = true
        _commentsLoading.value = false
    }
}
