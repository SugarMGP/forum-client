package org.jh.forum.client.data.api

import org.jh.forum.client.data.model.*

interface ForumApi {
    suspend fun login(request: LoginRequest): AjaxResult<LoginResponse>
    suspend fun logout(): AjaxResultVoid
    suspend fun getProfile(id: Long? = null): AjaxResult<GetUserProfileResponse>
    suspend fun updateMyProfile(request: UpdateUserProfileRequest): AjaxResult<ModerationResultResponse>
    suspend fun updateNoticeSettings(request: UpdateNoticeSettingsRequest): AjaxResultVoid
    suspend fun getNoticeSettings(): AjaxResult<GetNoticeSettingsResponse>
    suspend fun checkMute(): AjaxResult<CheckMuteResponse>

    suspend fun upvotePost(id: Long): AjaxResult<UpvotePostResponse>
    suspend fun topPost(request: TopPostRequest): AjaxResultVoid
    suspend fun createPost(request: PublishPostRequest): AjaxResult<ModerationResultResponse>
    suspend fun getPersonalPostList(
        page: Int,
        pageSize: Int,
        id: Long?
    ): AjaxResult<BaseListResponse<GetPersonalPostListElement>>

    suspend fun getPostList(
        page: Int,
        pageSize: Int,
        category: String?,
        sortType: String?
    ): AjaxResult<BaseListResponse<GetPostListElement>>

    suspend fun getPostInfo(id: Long): AjaxResult<GetPostInfoResponse>
    suspend fun deletePost(id: Long): AjaxResultVoid

    suspend fun upvoteComment(id: Long): AjaxResult<UpvoteCommentResponse>
    suspend fun publishComment(request: PublishCommentRequest): AjaxResult<ModerationResultResponse>
    suspend fun pinComment(id: Long): AjaxResult<PinCommentResponse>
    suspend fun getReplyList(
        page: Int,
        pageSize: Int,
        id: Long,
        highlightReplyId: Long?
    ): AjaxResult<GetCommentReplyListResponse>

    suspend fun getPersonalComment(
        page: Int,
        pageSize: Int
    ): AjaxResult<BaseListResponse<PersonalCommentListElement>>

    suspend fun getCommentList(
        page: Int,
        pageSize: Int,
        id: Long,
        sortType: String?,
        highlightCommentId: Long?
    ): AjaxResult<BaseListResponse<CommentElement>>

    suspend fun deleteComment(id: Long): AjaxResultVoid

    suspend fun getAnnouncementList(
        page: Int,
        pageSize: Int,
        type: String?
    ): AjaxResult<BaseListResponse<GetAnnouncementListElement>>

    suspend fun reportUser(request: ReportUserRequest): AjaxResultVoid
    suspend fun reportContent(request: ReportContentRequest): AjaxResultVoid

    suspend fun uploadPicture(bytes: ByteArray, filename: String): AjaxResult<UploadResponse>

    suspend fun checkUnread(): AjaxResult<UnreadCheckResponse>
    suspend fun getNoticeList(
        page: Int,
        pageSize: Int,
        type: Int
    ): AjaxResult<BaseListResponse<GetNoticeListElement>>
}
