package org.jh.forum.client.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.jh.forum.client.data.model.*

class KtorForumApi(private val client: HttpClient, private val baseUrl: String) : ForumApi {
    private fun url(path: String) = baseUrl.trimEnd('/') + path

    override suspend fun login(request: LoginRequest): AjaxResult<LoginResponse> =
        client.post(url("/api/user/login")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun logout(): AjaxResultVoid =
        client.post(url("/api/user/logout")) {
            contentType(ContentType.Application.Json)
        }.body()

    override suspend fun getProfile(id: Long?): AjaxResult<GetUserProfileResponse> =
        client.get(url("/api/user/profile")) {
            parameter("id", id)
        }.body()

    override suspend fun updateMyProfile(request: UpdateUserProfileRequest): AjaxResultVoid =
        client.put(url("/api/user/profile")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun updateNoticeSettings(request: UpdateNoticeSettingsRequest): AjaxResultVoid =
        client.post(url("/api/user/notice")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun getNoticeSettings(): AjaxResult<GetNoticeSettingsResponse> =
        client.get(url("/api/user/notice")).body()

    override suspend fun checkMute(): AjaxResult<CheckMuteResponse> =
        client.get(url("/api/user/mute")).body()

    override suspend fun upvotePost(id: Long): AjaxResult<UpvotePostResponse> =
        client.post(url("/api/post/upvote")) {
            parameter("id", id)
        }.body()

    override suspend fun topPost(request: TopPostRequest): AjaxResultVoid =
        client.post(url("/api/post/top")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun createPost(request: PublishPostRequest): AjaxResult<ModerationResultResponse> {
        try {
            val response = client.post(url("/api/post/create")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val result = response.body<AjaxResult<ModerationResultResponse>>()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getPersonalPostList(
        page: Int,
        pageSize: Int,
        id: Long?
    ): AjaxResult<BaseListResponse<GetPersonalPostListElement>> =
        client.get(url("/api/post/personal")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("id", id)
        }.body()

    override suspend fun getPostList(
        page: Int,
        pageSize: Int,
        category: String?,
        sortType: String?
    ): AjaxResult<BaseListResponse<GetPostListElement>> =
        client.get(url("/api/post/list")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("category", category)
            parameter("sortType", sortType)
        }.body()

    override suspend fun getPostInfo(id: Long): AjaxResult<GetPostInfoResponse> =
        client.get(url("/api/post/info")) {
            parameter("id", id)
        }.body()

    override suspend fun deletePost(id: Long): AjaxResultVoid =
        client.delete(url("/api/post/delete")) {
            parameter("id", id)
        }.body()

    override suspend fun upvoteComment(id: Long): AjaxResult<UpvoteCommentResponse> =
        client.post(url("/api/comment/upvote")) {
            parameter("id", id)
        }.body()

    override suspend fun publishComment(request: PublishCommentRequest): AjaxResult<ModerationResultResponse> =
        client.post(url("/api/comment/publish")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun pinComment(id: Long): AjaxResult<PinCommentResponse> =
        client.post(url("/api/comment/pin")) {
            parameter("id", id)
        }.body()

    override suspend fun getReplyList(
        page: Int,
        pageSize: Int,
        id: Long,
        highlightReplyId: Long?
    ): AjaxResult<GetCommentReplyListResponse> =
        client.get(url("/api/comment/reply/list")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("id", id)
            parameter("highlightReplyId", highlightReplyId)
        }.body()

    override suspend fun getPersonalComment(
        page: Int,
        pageSize: Int
    ): AjaxResult<BaseListResponse<PersonalCommentListElement>> =
        client.get(url("/api/comment/personal")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()

    override suspend fun getCommentList(
        page: Int,
        pageSize: Int,
        id: Long,
        sortType: String?,
        highlightCommentId: Long?
    ): AjaxResult<BaseListResponse<CommentElement>> =
        client.get(url("/api/comment/list")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("id", id)
            parameter("sortType", sortType)
            parameter("highlightCommentId", highlightCommentId)
        }.body()

    override suspend fun deleteComment(id: Long): AjaxResultVoid =
        client.delete(url("/api/comment/remove")) {
            parameter("id", id)
        }.body()

    override suspend fun getAnnouncementList(
        page: Int,
        pageSize: Int,
        type: String?
    ): AjaxResult<BaseListResponse<GetAnnouncementListElement>> =
        client.get(url("/api/announcements/list")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("type", type)
        }.body()

    override suspend fun reportUser(request: ReportUserRequest): AjaxResultVoid =
        client.post(url("/api/report/user")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun reportContent(request: ReportContentRequest): AjaxResultVoid =
        client.post(url("/api/report/content")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun uploadPicture(
        bytes: ByteArray,
        filename: String
    ): AjaxResult<UploadResponse> =
        client.submitFormWithBinaryData(
            url("/api/file/picture"),
            formData {
                append("picture", bytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "form-data; name=\"picture\"; filename=\"$filename\"")
                    append(HttpHeaders.ContentType, "image/${filename.substringAfterLast('.', "jpeg")}")
                })
            }
        ).body()

    override suspend fun checkUnread(): AjaxResult<UnreadCheckResponse> =
        client.get(url("/api/notices/unread")).body()

    override suspend fun getNoticeList(
        page: Int,
        pageSize: Int,
        type: Int
    ): AjaxResult<BaseListResponse<GetNoticeListElement>> =
        client.get(url("/api/notices/list")) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("type", type)
        }.body()
}