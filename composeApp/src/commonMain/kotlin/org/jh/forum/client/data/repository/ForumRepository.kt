package org.jh.forum.client.data.repository

import org.jh.forum.client.data.api.ForumApi
import org.jh.forum.client.data.model.*

class ForumRepository(private val api: ForumApi) {
    suspend fun login(request: LoginRequest): AjaxResult<LoginResponse> = api.login(request)
    suspend fun logout(): AjaxResultVoid = api.logout()
    suspend fun getProfile(id: Long? = null): AjaxResult<GetUserProfileResponse> =
        api.getProfile(id)

    suspend fun updateProfile(request: UpdateUserProfileRequest): AjaxResult<ModerationResultResponse> =
        api.updateMyProfile(request)

    suspend fun updateNoticeSettings(request: UpdateNoticeSettingsRequest): AjaxResultVoid =
        api.updateNoticeSettings(request)

    suspend fun getNoticeSettings(): AjaxResult<GetNoticeSettingsResponse> = api.getNoticeSettings()

    suspend fun checkMute(): AjaxResult<CheckMuteResponse> = api.checkMute()

    suspend fun upvotePost(id: Long): AjaxResult<UpvotePostResponse> = api.upvotePost(id)
    suspend fun topPost(request: TopPostRequest): AjaxResultVoid = api.topPost(request)
    suspend fun createPost(request: PublishPostRequest): AjaxResult<ModerationResultResponse> =
        api.createPost(request)

    suspend fun getPostList(
        page: Int,
        pageSize: Int,
        category: String?,
        sortType: String?
    ): AjaxResult<BaseListResponse<GetPostListElement>> =
        api.getPostList(page, pageSize, category, sortType)

    suspend fun getPostInfo(id: Long): AjaxResult<GetPostInfoResponse> = api.getPostInfo(id)
    suspend fun deletePost(id: Long): AjaxResultVoid = api.deletePost(id)

    suspend fun upvoteComment(id: Long): AjaxResult<UpvoteCommentResponse> = api.upvoteComment(id)
    suspend fun publishComment(request: PublishCommentRequest): AjaxResult<ModerationResultResponse> =
        api.publishComment(request)

    suspend fun pinComment(id: Long): AjaxResult<PinCommentResponse> = api.pinComment(id)
    suspend fun getReplyList(
        page: Int,
        pageSize: Int,
        id: Long,
        highlightReplyId: Long
    ): AjaxResult<GetCommentReplyListResponse> =
        api.getReplyList(page, pageSize, id, highlightReplyId)

    suspend fun getPersonalComment(
        page: Int,
        pageSize: Int
    ): AjaxResult<BaseListResponse<PersonalCommentListElement>> =
        api.getPersonalComment(page, pageSize)

    suspend fun getCommentList(
        page: Int,
        pageSize: Int,
        id: Long,
        sortType: String?,
        highlightCommentId: Long
    ): AjaxResult<BaseListResponse<CommentElement>> =
        api.getCommentList(page, pageSize, id, sortType, highlightCommentId)

    suspend fun deleteComment(id: Long): AjaxResultVoid = api.deleteComment(id)

    suspend fun getAnnouncementList(
        page: Int,
        pageSize: Int,
        type: String?
    ): AjaxResult<BaseListResponse<GetAnnouncementListElement>> =
        api.getAnnouncementList(page, pageSize, type)

    suspend fun reportUser(request: ReportUserRequest): AjaxResultVoid = api.reportUser(request)
    suspend fun reportContent(request: ReportContentRequest): AjaxResultVoid =
        api.reportContent(request)

    suspend fun uploadPicture(bytes: ByteArray, filename: String): AjaxResult<UploadResponse> =
        api.uploadPicture(bytes, filename)

    suspend fun checkUnread(): AjaxResult<UnreadCheckResponse> = api.checkUnread()

    suspend fun getPersonalPostList(
        page: Int,
        pageSize: Int,
        userId: Long?
    ): AjaxResult<BaseListResponse<GetPersonalPostListElement>> =
        api.getPersonalPostList(page, pageSize, userId)

    suspend fun getNoticeList(
        page: Int,
        pageSize: Int,
        type: Int
    ): AjaxResult<BaseListResponse<GetNoticeListElement>> =
        api.getNoticeList(page, pageSize, type)
}