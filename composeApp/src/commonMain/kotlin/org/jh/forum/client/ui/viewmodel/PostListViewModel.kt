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

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NEWEST)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    private val _totalPages = MutableStateFlow(1)

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    fun loadPosts(category: String? = null, sortType: String? = null, reset: Boolean = false) {
        val actualSortType = sortType ?: this._sortType.value.name.lowercase()
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            if (reset) {
                _currentPage.value = 1
                _posts.value = emptyList()
                _hasMore.value = true
            }

            try {
                val result = repository.getPostList(_currentPage.value, 20, category, actualSortType)
                _isLoading.value = false

                if (result.code == 200 && result.data != null) {
                    val response = result.data
                    if (response.list.isNotEmpty()) {
                        if (reset) {
                            _posts.value = response.list
                        } else {
                            _posts.value = _posts.value + response.list
                        }

                        _totalPages.value = ((response.total + 19) / 20).toInt() // 计算总页数
                        _currentPage.value = _currentPage.value + 1
                        _hasMore.value = _currentPage.value <= _totalPages.value
                    } else {
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
                            val updatedLikeCount = if (updatedLiked) post.likeCount + 1 else maxOf(0, post.likeCount - 1)
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

    fun refresh() {
        loadPosts(category = _selectedCategory.value, sortType = _sortType.value.name.lowercase(), reset = true)
    }

    fun selectCategory(category: String?) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            loadPosts(category = category, sortType = _sortType.value.name.lowercase(), reset = true)
        }
    }

    fun setSortType(sortType: SortType) {
        if (_sortType.value != sortType) {
            _sortType.value = sortType
            loadPosts(category = _selectedCategory.value, sortType = sortType.name.lowercase(), reset = true)
        }
    }
}