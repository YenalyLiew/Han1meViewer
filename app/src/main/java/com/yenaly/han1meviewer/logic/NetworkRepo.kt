package com.yenaly.han1meviewer.logic

import android.util.Log
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.logic.exception.CloudFlareBlockedException
import com.yenaly.han1meviewer.logic.exception.HanimeNotFoundException
import com.yenaly.han1meviewer.logic.exception.IPBlockedException
import com.yenaly.han1meviewer.logic.exception.ParseException
import com.yenaly.han1meviewer.logic.model.CommentPlace
import com.yenaly.han1meviewer.logic.model.MyListType
import com.yenaly.han1meviewer.logic.model.VideoCommentArguments
import com.yenaly.han1meviewer.logic.model.VideoCommentModel
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
        sort: String?, broad: Boolean, year: Int?, month: Int?,
        duration: String?, tags: Set<String>, brands: Set<String>,
    ) = pageIOFlow(
        request = {
            HanimeNetwork.hanimeService.getHanimeSearchResult(
                page, query, genre, sort,
                if (broad) "on" else null,
                year, month, duration, tags, brands
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
        likeStatus: Boolean, // false => "": add fav, true => "1": cancel fav
        currentUserId: String?,
        token: String?,
    ) = websiteIOFlow(
        request = {
            HanimeNetwork.hanimeService.addToMyFavVideo(
                videoCode, if (likeStatus) "1" else EMPTY_STRING,
                token, currentUserId
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
        text: String,
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
        text: String,
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

    fun likeComment(
        csrfToken: String?,
        commentPlace: CommentPlace,
        foreignId: String?,
        isPositive: Boolean, // 你選擇的是讚還是踩，1是讚，0是踩
        likeUserId: String?,
        commentLikesCount: Int,
        commentLikesSum: Int,
        likeCommentStatus: Boolean, // 你之前有沒有點過讚，1是0否
        unlikeCommentStatus: Boolean, // 你之前有沒有點過踩，1是0否
        commentPosition: Int, comment: VideoCommentModel.VideoComment,
    ) = websiteIOFlow(
        request = {
            HanimeNetwork.commentService.likeComment(
                csrfToken, commentPlace.value, foreignId,
                if (isPositive) 1 else 0,
                likeUserId, commentLikesCount, commentLikesSum,
                if (likeCommentStatus) 1 else 0,
                if (unlikeCommentStatus) 1 else 0
            )
        }
    ) {
        Log.d("like_comment_body", it)
        return@websiteIOFlow WebsiteState.Success(
            VideoCommentArguments(
                commentPosition, isPositive, comment
            )
        )
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
        if (requestResult.isSuccessful && resultBody != null) {
            emit(action.invoke(resultBody))
        } else {
            requestResult.throwRequestException()
        }
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            is ParseException -> {
                e.printStackTrace()
                emit(WebsiteState.Error(ParseException("可能這個網址解析起來不大一樣...")))
            }

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
        if (requestResult.isSuccessful && resultBody != null) {
            emit(action.invoke(resultBody))
        } else {
            requestResult.throwRequestException()
        }
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            is ParseException -> {
                e.printStackTrace()
                emit(PageLoadingState.Error(ParseException("可能這個網址解析起來不大一樣...")))
            }

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
        if (requestResult.isSuccessful && resultBody != null) {
            emit(action.invoke(resultBody))
        } else {
            requestResult.throwRequestException()
        }
    }.catch { e ->
        when (e) {
            is CancellationException -> throw e
            is ParseException -> {
                e.printStackTrace()
                emit(VideoLoadingState.Error(ParseException("可能這個網址解析起來不大一樣...")))
            }

            else -> {
                e.printStackTrace()
                emit(VideoLoadingState.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun Response<ResponseBody>.throwRequestException(): Nothing {
        val body = errorBody()?.string()
        when (val code = code()) {
            403 -> if (!body.isNullOrBlank()) {
                when {
                    "you have been blocked" in body ->
                        throw IPBlockedException("不要使用日本IP地址!!!")

                    "Just a moment" in body ->
                        throw CloudFlareBlockedException("看到這裏説明他們網站加固了，能不能恢復只能聽天命了...")

                    else ->
                        throw HanimeNotFoundException("可能不存在") // 主要出現在影片界面，當你v數不大時會報403
                }
            } else throw IllegalStateException("$code ${message()}")

            500 -> throw HanimeNotFoundException("可能不存在") // 主要出現在影片界面，當你v數很大時會報500

            else -> throw IllegalStateException("$code ${message()}")
        }
    }
}