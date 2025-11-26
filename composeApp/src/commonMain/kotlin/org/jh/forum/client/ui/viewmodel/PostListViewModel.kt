package org.jh.forum.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.GetPostListElement
import org.jh.forum.client.data.model.SortType
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.di.AppModule

class PostListViewModel : ViewModel() {
    private val repository: ForumRepository = AppModule.forumRepository
    private val _posts = MutableStateFlow<List<GetPostListElement>>(emptyList())
    val posts: StateFlow<List<GetPostListElement>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    private val _totalPages = MutableStateFlow(1)

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    fun loadPosts(
        category: String? = null,
        sortType: SortType = SortType.NEWEST,
        reset: Boolean = false
    ) {
        val actualSortType = sortType.name.lowercase()
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Store the page to load before modifying state
            val pageToLoad = if (reset) 1 else _currentPage.value

            try {
                val result =
                    repository.getPostList(pageToLoad, 20, category, actualSortType)
                _isLoading.value = false

                if (result.code == 200 && result.data != null) {
                    val response = result.data
                    if (response.list.isNotEmpty()) {
                        if (reset) {
                            // Replace the entire list with new data
                            _posts.value = response.list
                            _currentPage.value = 2 // Next page to load
                            _hasMore.value = true
                        } else {
                            // Merge new posts with existing ones, filtering out duplicates
                            val existingIds = _posts.value.map { it.id }.toSet()
                            val uniqueNewPosts = response.list.filter { it.id !in existingIds }
                            _posts.value = _posts.value + uniqueNewPosts
                            _currentPage.value = _currentPage.value + 1
                        }

                        _totalPages.value = ((response.total + 19) / 20).toInt() // 计算总页数
                        _hasMore.value = _currentPage.value <= _totalPages.value
                    } else {
                        if (reset) {
                            // Only clear posts if we got empty result on reset
                            _posts.value = emptyList()
                        }
                        _errorMessage.value = "没有找到帖子"
                    }
                } else {
                    _errorMessage.value = result.msg ?: "加载失败"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "加载失败"
                e.printStackTrace()
            }
        }
    }

    fun upvotePost(postId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.upvotePost(postId)
                if (result.code == 200 && result.data != null) {
                    val updatedPosts = _posts.value.map { post ->
                        if (post.id == postId) {
                            val updatedLiked = result.data.status
                            val updatedLikeCount =
                                if (updatedLiked) post.likeCount + 1 else maxOf(
                                    0,
                                    post.likeCount - 1
                                )
                            post.copy(
                                likeCount = updatedLikeCount,
                                isLiked = updatedLiked
                            )
                        } else {
                            post
                        }
                    }
                    _posts.value = updatedPosts
                } else {
                    _errorMessage.value = result.msg ?: "点赞失败"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "点赞失败"
            }
        }
    }

    fun refresh(category: String?, sortType: SortType) {
        loadPosts(
            category = category,
            sortType = sortType,
            reset = true
        )
    }

    /**
     * Update a specific post's like status and count in the list
     * Called when returning from post detail screen to sync the like state
     */
    fun updatePostLikeStatus(postId: Long, isLiked: Boolean, likeCount: Int) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isLiked = isLiked, likeCount = likeCount)
            } else {
                post
            }
        }
    }

    /**
     * Remove a post from the list when it's deleted
     * Called when a post is deleted from the detail screen
     */
    fun removePost(postId: Long) {
        _posts.value = _posts.value.filterNot { it.id == postId }
    }
}
