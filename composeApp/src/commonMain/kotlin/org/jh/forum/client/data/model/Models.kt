package org.jh.forum.client.data.model

import kotlinx.serialization.Serializable

// Generic AjaxResult wrapper
@Serializable
data class AjaxResult<T>(
    val code: Int,
    val msg: String? = null,
    val data: T? = null
)

@Serializable
data class AjaxResultVoid(
    val code: Int,
    val msg: String? = null,
    val data: Unit? = null
)

// Generic paginated list response
@Serializable
data class BaseListResponse<T>(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val list: List<T> = emptyList()
)

@Serializable
data class PictureInfoDTO(
    val url: String? = null,
    val thumbnailUrl: String? = null
)

@Serializable
data class UserInfoDTO(
    val id: Long? = null,
    val nickname: String? = null,
    val avatar: String? = null
)

@Serializable
data class GetPostListElement(
    val id: Long,
    val publisherInfo: UserInfoDTO = UserInfoDTO(),
    val category: String = "",
    val topics: List<String> = emptyList(),
    val title: String? = null,
    val content: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String = "",
    val isPinned: Boolean = false,
    val pictures: List<PictureInfoDTO> = emptyList(),
    val totalPictures: Int = 0,
    val isLiked: Boolean = false
)

@Serializable
data class GetPersonalPostListElement(
    val id: Long,
    val category: String = "",
    val topics: List<String> = emptyList(),
    val title: String? = null,
    val content: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: String = "",
    val isLiked: Boolean = false,
    val isTopped: Boolean = false,
    val pictures: List<PictureInfoDTO> = emptyList(),
    val totalPictures: Int = 0,
    val publisherInfo: UserInfoDTO = UserInfoDTO()
)

@Serializable
data class GetPostInfoResponse(
    val publisherInfo: UserInfoDTO = UserInfoDTO(),
    val category: String = "",
    val topics: List<String> = emptyList(),
    val title: String? = null,
    val content: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: String = "",
    val pictures: List<PictureInfoDTO> = emptyList(),
    val isLiked: Boolean = false
)

@Serializable
data class UpvotePostResponse(
    val status: Boolean = false
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val userType: String? = null,
    val userInfo: OauthUserInfoElement? = null
)

@Serializable
data class OauthUserInfoElement(
    val gender: String? = null,
    val name: String? = null,
    val studentId: String? = null,
    val studentType: String? = null
)

@Serializable
data class GetUserProfileResponse(
    val userId: Long? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val signature: String? = null,
    val profile: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val background: String? = null,
    val realnameVisible: Boolean? = null,
    val studentIdVisible: Boolean? = null,
    val birthdayVisible: Boolean? = null,
    val realname: String? = null,
    val studentId: String? = null,
    val collegeId: String? = null,
    val birthday: String? = null
)

@Serializable
data class UpdateUserProfileRequest(
    val avatar: String,
    val nickname: String,
    val signature: String,
    val gender: String,
    val profile: String,
    val email: String,
    val collegeId: String,
    val birthday: String? = null,
    val birthdayVisible: Boolean,
    val realnameVisible: Boolean,
    val studentIdVisible: Boolean
)

@Serializable
data class UpdateNoticeSettingsRequest(
    val upvoteNotice: Boolean,
    val commentNotice: Boolean
)

@Serializable
data class GetNoticeListElement(
    val id: Long,
    val senderInfo: UserInfoDTO = UserInfoDTO(),
    val type: String = "",
    val positionType: String? = null,
    val postId: Long? = null,
    val commentId: Long? = null,
    val replyId: Long? = null,
    val positionContent: String? = null,
    val newCommentId: Long? = null,
    val newCommentContent: String? = null,
    val updatedAt: String = "",
    val isLiked: Boolean? = null,
    val isRead: Boolean = true
)

@Serializable
data class UnreadCheckResponse(
    val unreadNoticeCount: Int = 0,
    val unreadAnnouncementCount: Int = 0
)

// Simple wrapper for upload response
@Serializable
data class UploadResponse(
    val pass: Boolean = false,
    val url: String? = null
)

// Additional models referenced by the API interface
@Serializable
data class GetNoticeSettingsResponse(
    val upvoteNotice: Boolean = false,
    val commentNotice: Boolean = false
)

@Serializable
data class CheckMuteResponse(
    val mutedUntil: String? = null
)

@Serializable
data class TopPostRequest(
    val id: Long,
    val topped: Boolean
)

@Serializable
data class PublishPostRequest(
    val category: String,
    val content: String,
    val pictures: List<String> = emptyList(),
    val title: String,
    val topics: List<String> = emptyList()
)

// --- move dependent types here so they are visible when used below ---
@Serializable
data class Label(
    val description: String? = null,
    val keywords: String? = null
)

@Serializable
data class CommentInfoResponse(
    val commentId: Long = 0,
    val publisherInfo: UserInfoDTO = UserInfoDTO(),
    val content: String = "",
    val pictures: List<PictureInfoDTO> = emptyList(),
    val isPinned: Boolean = false,
    val isAuthor: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: String = "",
    val upvoteCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false
)

@Serializable
data class ReplyElement(
    val replyId: Long = 0,
    val publisherInfo: UserInfoDTO = UserInfoDTO(),
    val targetUser: UserInfoDTO? = null,
    val content: String = "",
    val pictures: List<PictureInfoDTO> = emptyList(),
    val isPinned: Boolean = false,
    val isAuthor: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: String = "",
    val upvoteCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false
)

@Serializable
data class ModerationResultResponse(
    val pass: Boolean = true,
    val labels: List<Label> = emptyList(),
    val requestId: String? = null
)

@Serializable
data class PublishCommentRequest(
    val targetType: String,
    val targetId: Long,
    val content: String,
    val picture: String = ""
)

@Serializable
data class UpvoteCommentResponse(
    val status: Boolean = false
)

@Serializable
data class PinCommentResponse(
    val status: Boolean = false
)

@Serializable
data class GetCommentReplyListResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Long = 0,
    val list: List<ReplyElement> = emptyList(),
    val commentInfo: CommentInfoResponse? = null
)

@Serializable
data class PersonalCommentListElement(
    val postId: Long = 0,
    val commentId: Long = 0,
    val replyId: Long = 0,
    val targetContent: String? = null,
    val content: String = "",
    val pictures: List<PictureInfoDTO> = emptyList(),
    val createdAt: String = "",
    val isLiked: Boolean = false,
    val upvoteCount: Int = 0,
    val replyCount: Int = 0
)

@Serializable
data class CommentElement(
    val commentId: Long = 0,
    val publisherInfo: UserInfoDTO = UserInfoDTO(),
    val content: String = "",
    val pictures: List<PictureInfoDTO> = emptyList(),
    val isPinned: Boolean = false,
    val isAuthor: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: String = "",
    val upvoteCount: Int = 0,
    val replyCount: Int = 0,
    val replies: List<ReplyElement> = emptyList(),
    val isLiked: Boolean = false
)

@Serializable
data class GetAnnouncementListElement(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val type: String = "",
    val signatory: String? = null,
    val publishedAt: String = "",
    val sticky: Boolean = false,
    val isRead: Boolean = false
)

@Serializable
data class ReportUserRequest(
    val type: String,
    val reason: String,
    val userId: Long,
    val pictures: List<String> = emptyList()
)

@Serializable
data class ReportContentRequest(
    val target: String,
    val type: String,
    val reason: String,
    val targetId: Long,
    val pictures: List<String> = emptyList()
)

enum class PostCategory(val value: String, val displayName: String) {
    CAMPUS("campus", "校园"),
    EMOTION("emotion", "情感"),
    STUDY("study", "学习"),
    CONTEST("contest", "比赛"),
    HOBBY("hobby", "爱好"),
    LOST("lost", "失物"),
    SECONDHAND("secondhand", "二手");

    companion object {
        fun getDisplayName(value: String): String {
            return entries.find { it.value == value }?.displayName ?: "全部"
        }
    }
}

enum class SortType {
    HOT,
    NEWEST
}