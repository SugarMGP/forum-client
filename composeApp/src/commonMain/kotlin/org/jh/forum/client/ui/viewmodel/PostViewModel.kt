package org.jh.forum.client.ui.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.GetPostInfoResponse
import org.jh.forum.client.data.model.PublishPostRequest
import org.jh.forum.client.di.AppModule
import java.io.InputStream

@Stable
class PostViewModel : ViewModel() {
    private val repository = AppModule.forumRepository

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun getPost(postId: Long, onResult: (GetPostInfoResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.getPostInfo(postId)
                if (result.code == 200 && result.data != null) {
                    onResult(result.data)
                } else {
                    _errorMessage.value = result.msg ?: "获取帖子失败"
                    onResult(null)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "获取帖子失败"
                onResult(null)
            }
        }
    }

    fun upvotePost(postId: Long, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = repository.upvotePost(postId)
                if (result.code == 200 && result.data != null) {
                    onResult(result.data.status)
                } else {
                    _errorMessage.value = result.msg ?: "点赞失败"
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "点赞失败"
                onResult(false)
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.deletePost(postId)
                if (result.code != 0) {
                    _errorMessage.value = result.msg ?: "删除失败"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "删除失败"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun createPost(
        title: String,
        content: String,
        category: String,
        topics: List<String>,
        pictures: List<String>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = PublishPostRequest(
                    title = title,
                    content = content,
                    category = category,
                    topics = topics,
                    pictures = pictures
                )

                val result = repository.createPost(request)
                if (result.code == 200) {
                    onResult(true)
                } else {
                    _errorMessage.value = result.msg ?: "发布失败"
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "发布失败"
                onResult(false)
                e.printStackTrace()
            }
        }
    }

    fun uploadImage(input: InputStream, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.uploadPicture(input)
                if (result.code == 200 && result.data != null && result.data.url != null) {
                    onResult(result.data.url)
                } else {
                    _errorMessage.value = result.msg ?: "图片上传失败"
                    onResult(null)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "图片上传失败"
                onResult(null)
            } finally {
                try { input.close() } catch (_: Exception) {}
            }
        }
    }
}