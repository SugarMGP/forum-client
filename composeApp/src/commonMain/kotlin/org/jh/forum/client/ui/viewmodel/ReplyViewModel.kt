package org.jh.forum.client.ui.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.CommentInfoResponse
import org.jh.forum.client.data.model.PublishCommentRequest
import org.jh.forum.client.data.model.ReplyElement
import org.jh.forum.client.di.AppModule

@Stable
class ReplyViewModel : ViewModel() {
    private val repository = AppModule.forumRepository

    private val _commentInfo = MutableStateFlow<CommentInfoResponse?>(null)
    val commentInfo: StateFlow<CommentInfoResponse?> = _commentInfo.asStateFlow()

    private val _replies = MutableStateFlow<List<ReplyElement>>(emptyList())
    val replies: StateFlow<List<ReplyElement>> = _replies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _highlightReplyId = MutableStateFlow(0L)
    val highlightReplyId: StateFlow<Long> = _highlightReplyId.asStateFlow()

    fun setHighlightReplyId(highlightId: Long?) {
        _highlightReplyId.value = highlightId ?: 0L
    }

    fun loadReplies(commentId: Long, reset: Boolean = false, highlightId: Long? = null) {
        if (reset) {
            _currentPage.value = 1
            _replies.value = emptyList()
            _hasMore.value = true
            _highlightReplyId.value = highlightId ?: 0L
        }

        if (!_hasMore.value || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getReplyList(
                    page = _currentPage.value,
                    pageSize = 20,
                    id = commentId,
                    highlightReplyId = _highlightReplyId.value
                )
                _isLoading.value = false
                if (result.code == 200 && result.data != null) {
                    val response = result.data

                    // Store comment info
                    _commentInfo.value = response.commentInfo

                    val newReplies = if (reset) {
                        response.list
                    } else {
                        // Merge new replies with existing ones, filtering out duplicates
                        val existingIds = _replies.value.map { it.replyId }.toSet()
                        val uniqueNewReplies = response.list.filter { it.replyId !in existingIds }
                        _replies.value + uniqueNewReplies
                    }
                    _replies.value = newReplies
                    _hasMore.value = response.page * response.pageSize < response.total
                    if (_hasMore.value) _currentPage.value++
                } else {
                    _errorMessage.value = result.msg ?: "加载失败"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "加载失败"
            }
        }
    }

    fun publishReply(commentId: Long, targetId: Long, content: String, picture: String? = null) {
        viewModelScope.launch {
            val request = PublishCommentRequest(
                targetType = "comment",
                targetId = targetId,
                content = content,
                picture = picture ?: ""
            )

            val result = repository.publishComment(request)
            if (result.code == 200 && result.data != null) {
                loadReplies(commentId, true, 0L)
            } else {
                _errorMessage.value = result.msg ?: "发布失败"
            }
        }
    }

    fun upvoteComment(commentId: Long) {
        viewModelScope.launch {
            val result = repository.upvoteComment(commentId)
            if (result.code == 200 && result.data != null) {
                // Update comment like status
                _commentInfo.value?.let { comment ->
                    _commentInfo.value = comment.copy(
                        upvoteCount = if (result.data.status) {
                            comment.upvoteCount + 1
                        } else {
                            comment.upvoteCount - 1
                        },
                        isLiked = result.data.status
                    )
                }
            } else {
                _errorMessage.value = result.msg ?: "点赞失败"
            }
        }
    }

    fun upvoteReply(replyId: Long) {
        viewModelScope.launch {
            val result = repository.upvoteComment(replyId)
            if (result.code == 200 && result.data != null) {
                // Update reply like status
                val updatedReplies = _replies.value.map { reply ->
                    if (reply.replyId == replyId) {
                        reply.copy(
                            upvoteCount = if (result.data.status) {
                                reply.upvoteCount + 1
                            } else {
                                reply.upvoteCount - 1
                            },
                            isLiked = result.data.status
                        )
                    } else {
                        reply
                    }
                }
                _replies.value = updatedReplies
            } else {
                _errorMessage.value = result.msg ?: "点赞失败"
            }
        }
    }

    fun deleteReply(replyId: Long) {
        viewModelScope.launch {
            val result = repository.deleteComment(replyId)
            if (result.code == 200) {
                // Remove reply from list
                _replies.value = _replies.value.filter { it.replyId != replyId }
            } else {
                _errorMessage.value = result.msg ?: "删除失败"
            }
        }
    }
}
