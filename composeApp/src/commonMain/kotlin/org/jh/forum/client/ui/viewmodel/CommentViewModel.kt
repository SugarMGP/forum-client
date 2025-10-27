package org.jh.forum.client.ui.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.CommentElement
import org.jh.forum.client.data.model.PublishCommentRequest
import org.jh.forum.client.di.AppModule

@Stable
class CommentViewModel : ViewModel() {
    private val repository = AppModule.forumRepository

    private val _comments = MutableStateFlow<List<CommentElement>>(emptyList())
    val comments: StateFlow<List<CommentElement>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _highlightCommentId = MutableStateFlow<Long?>(null)
    val highlightCommentId: StateFlow<Long?> = _highlightCommentId.asStateFlow()

    fun loadComments(postId: Long, reset: Boolean = false, highlightId: Long? = null) {
        if (reset) {
            _currentPage.value = 1
            _comments.value = emptyList()
            _hasMore.value = true
            _highlightCommentId.value = highlightId
        }

        if (!_hasMore.value || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getCommentList(
                    page = _currentPage.value,
                    pageSize = 20,
                    id = postId,
                    sortType = "time",
                    highlightCommentId = _highlightCommentId.value
                )
                _isLoading.value = false
                if (result.code == 200 && result.data != null) {
                    val response = result.data
                    val newComments = if (reset) {
                        response.list
                    } else {
                        // Merge new comments with existing ones, filtering out duplicates
                        val existingIds = _comments.value.map { it.commentId }.toSet()
                        val uniqueNewComments = response.list.filter { it.commentId !in existingIds }
                        _comments.value + uniqueNewComments
                    }
                    _comments.value = newComments
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

    fun publishComment(postId: Long, content: String, picture: String? = null) {
        viewModelScope.launch {
            val request = PublishCommentRequest(
                targetType = "post",
                targetId = postId,
                content = content,
                picture = picture ?: ""
            )

            val result = repository.publishComment(request)
            if (result.code == 200 && result.data != null) {
                // 重新加载评论列表，保留highlightCommentId
                loadComments(postId, true, _highlightCommentId.value)
            } else {
                _errorMessage.value = result.msg ?: "发布失败"
            }
        }
    }

    fun upvoteComment(commentId: Long) {
        viewModelScope.launch {
            val result = repository.upvoteComment(commentId)
            if (result.code == 200 && result.data != null) {
                // 更新评论点赞状态
                val updatedComments = _comments.value.map { comment ->
                    if (comment.commentId == commentId) {
                        CommentElement(
                            commentId = comment.commentId,
                            publisherInfo = comment.publisherInfo,
                            content = comment.content,
                            pictures = comment.pictures,
                            isPinned = comment.isPinned,
                            isAuthor = comment.isAuthor,
                            isDeleted = comment.isDeleted,
                            createdAt = comment.createdAt,
                            upvoteCount = if (result.data?.status == true) {
                                comment.upvoteCount + 1
                            } else {
                                comment.upvoteCount - 1
                            },
                            replyCount = comment.replyCount,
                            replies = comment.replies,
                            isLiked = result.data.status
                        )
                    } else {
                        comment
                    }
                }
                _comments.value = updatedComments
            } else {
                _errorMessage.value = result.msg ?: "点赞失败"
            }
        }
    }

    fun pinComment(commentId: Long) {
        viewModelScope.launch {
            val result = repository.pinComment(commentId)
            if (result.code == 200 && result.data != null) {
                // 更新评论置顶状态
                val updatedComments = _comments.value.map { comment ->
                    if (comment.commentId == commentId) {
                        CommentElement(
                            commentId = comment.commentId,
                            publisherInfo = comment.publisherInfo,
                            content = comment.content,
                            pictures = comment.pictures,
                            isPinned = result.data.status,
                            isAuthor = comment.isAuthor,
                            isDeleted = comment.isDeleted,
                            createdAt = comment.createdAt,
                            upvoteCount = comment.upvoteCount,
                            replyCount = comment.replyCount,
                            replies = comment.replies,
                            isLiked = comment.isLiked
                        )
                    } else {
                        CommentElement(
                            commentId = comment.commentId,
                            publisherInfo = comment.publisherInfo,
                            content = comment.content,
                            pictures = comment.pictures,
                            isPinned = false, // 取消其他评论的置顶状态
                            isAuthor = comment.isAuthor,
                            isDeleted = comment.isDeleted,
                            createdAt = comment.createdAt,
                            upvoteCount = comment.upvoteCount,
                            replyCount = comment.replyCount,
                            replies = comment.replies,
                            isLiked = comment.isLiked
                        )
                    }
                }
                _comments.value = updatedComments
            } else {
                _errorMessage.value = result.msg ?: "置顶失败"
            }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            val result = repository.deleteComment(commentId)
            if (result.code == 200) {
                // 从列表中移除评论
                _comments.value = _comments.value.filter { it.commentId != commentId }
            } else {
                _errorMessage.value = result.msg ?: "删除失败"
            }
        }
    }

    fun clearComments() {
        _comments.value = emptyList()
        _currentPage.value = 1
        _hasMore.value = true
        _highlightCommentId.value = null
        _errorMessage.value = null
    }
}