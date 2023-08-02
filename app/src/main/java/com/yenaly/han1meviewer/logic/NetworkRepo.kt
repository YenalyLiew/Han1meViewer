package com.yenaly.han1meviewer.logic

import android.util.Log
import com.yenaly.han1meviewer.FavStatus
import com.yenaly.han1meviewer.MyListType
import com.yenaly.han1meviewer.logic.network.HanimeNetwork
import com.yenaly.han1meviewer.logic.network.Parse
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:38
 */
object NetworkRepo {

    fun getHomePage() = websiteIOFlow(
        request = { HanimeNetwork.hanimeService.getHomePage() },
        action = Parse::homePageVer2
    )

    fun getHanimeSearchTags() = websiteIOFlow(
        request = { HanimeNetwork.hanimeService.getHanimeSearchResult() },
        action = Parse::hanimeSearchTags
    )

    fun getHanimeSearchResult(
        page: Int, query: String?, genre: String?,
        sort: String?, broad: String?, year: Int?, month: Int?,
        duration: String?, tags: LinkedHashSet<String>, brands: LinkedHashSet<String>,
    ) = pageIOFlow(
        request = {
            HanimeNetwork.hanimeService.getHanimeSearchResult(
                page, query, genre,
                sort, broad, year, month,
                duration, tags, brands
            )
        },
        action = Parse::hanimeSearch
    )

    fun getHanimeVideo(videoCode: String) = videoIOFlow(
        request = { HanimeNetwork.hanimeService.getHanimeVideo(videoCode) },
        action = Parse::hanimeVideoVer2
    )

    fun getHanimePreview(date: String) = websiteIOFlow(
        request = { HanimeNetwork.hanimeService.getHanimePreview(date) },
        action = Parse::hanimePreview
    )

    fun getMyList(page: Int, listType: MyListType) = pageIOFlow(
        request = { HanimeNetwork.hanimeService.getMyList(page, listType.value) },
        action = Parse::myList
    )

    fun deleteMyList(listType: MyListType, videoCode: String, token: String?) = websiteIOFlow(
        request = {
            HanimeNetwork.hanimeService.deleteMyList(
                listType.value, videoCode,
                csrfToken = token
            )
        }
    ) { deleteBody ->
        val jsonObject = JSONObject(deleteBody)
        val returnVideoCode = jsonObject.get("video_id").toString()
        if (videoCode == returnVideoCode) {
            return@websiteIOFlow WebsiteState.Success(Unit)
        }

        return@websiteIOFlow WebsiteState.Error(IllegalStateException("cannot delete it ?!"))
    }

    fun addToMyFavVideo(
        videoCode: String,
        likeStatus: FavStatus,
        currentUserId: String?,
        token: String?
    ) = websiteIOFlow(
        request = {
            HanimeNetwork.hanimeService.addToMyFavVideo(
                videoCode, likeStatus.value, token, currentUserId
            )
        }
    ) {
        Log.d("add_to_fav_body", it)
        return@websiteIOFlow WebsiteState.Success(Unit)
    }

    // ------ COMMENT ------ //

    fun getComments(type: String, code: String) = websiteIOFlow(
        request = { HanimeNetwork.commentService.getComments(type, code) },
        action = Parse::comments
    )

    fun getCommentReply(commentId: String) = websiteIOFlow(
        request = { HanimeNetwork.commentService.getCommentReply(commentId) },
        action = Parse::commentReply
    )

    fun postComment(
        csrfToken: String?,
        currentUserId: String,
        targetUserId: String,
        type: String,
        text: String
    ) = websiteIOFlow(
        request = {
            HanimeNetwork.commentService.postComment(
                csrfToken, currentUserId,
                type, targetUserId, text
            )
        }
    ) {
        Log.d("post_comment_body", it)
        return@websiteIOFlow WebsiteState.Success(Unit)
    }

    fun postCommentReply(
        csrfToken: String?,
        replyCommentId: String,
        text: String
    ) = websiteIOFlow(
        request = {
            HanimeNetwork.commentService.postCommentReply(
                csrfToken, replyCommentId, text
            )
        }
    ) {
        Log.d("post_comment_reply_body", it)
        return@websiteIOFlow WebsiteState.Success(Unit)
    }

    // ------ VERSION ------ //

    fun getLatestVersion() = flow {
        emit(WebsiteState.Loading)
        val versionInfo = HanimeNetwork.versionService.getLatestVersion()
        emit(WebsiteState.Success(versionInfo))
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            else -> {
                e.printStackTrace()
                emit(WebsiteState.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    // ------ BASE ------ //

    private fun <T> websiteIOFlow(
        request: suspend () -> Response<ResponseBody>,
        action: (String) -> WebsiteState<T>,
    ) = flow {
        emit(WebsiteState.Loading)
        val requestResult = request.invoke()
        val resultBody = requestResult.body()?.string()
        if (resultBody != null) {
            emit(action.invoke(resultBody))
        } else {
            emit(WebsiteState.Error(IllegalStateException("${requestResult.code()} ${requestResult.message()}")))
        }
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            else -> {
                e.printStackTrace()
                emit(WebsiteState.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun <T> pageIOFlow(
        request: suspend () -> Response<ResponseBody>,
        action: (String) -> PageLoadingState<T>,
    ) = flow {
        val requestResult = request.invoke()
        val resultBody = requestResult.body()?.string()
        if (resultBody != null) {
            emit(action.invoke(resultBody))
        } else {
            emit(PageLoadingState.Error(IllegalStateException("${requestResult.code()} ${requestResult.message()}")))
        }
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            else -> {
                e.printStackTrace()
                emit(PageLoadingState.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun <T> videoIOFlow(
        request: suspend () -> Response<ResponseBody>,
        action: (String) -> VideoLoadingState<T>,
    ) = flow {
        val requestResult = request.invoke()
        val resultBody = requestResult.body()?.string()
        if (resultBody != null) {
            emit(action.invoke(resultBody))
        } else {
            if (requestResult.code() == 403) {
                emit(VideoLoadingState.NoContent)
            } else {
                emit(VideoLoadingState.Error(IllegalStateException("${requestResult.code()} ${requestResult.message()}")))
            }
        }
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            is IndexOutOfBoundsException -> {
                e.printStackTrace()
                emit(VideoLoadingState.Error(IndexOutOfBoundsException("可能這個網址解析起來不大一樣...")))
            }

            else -> {
                e.printStackTrace()
                emit(VideoLoadingState.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)
}