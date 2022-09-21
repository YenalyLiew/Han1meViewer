package com.yenaly.han1meviewer.logic

import android.util.Log
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.FavStatus
import com.yenaly.han1meviewer.MyListType
import com.yenaly.han1meviewer.logic.model.*
import com.yenaly.han1meviewer.logic.network.HanimeNetwork
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.utils.isInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Response

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:38
 */
object NetworkRepo {

    fun getHomePage() = websiteIOFlow(
        request = { HanimeNetwork.hanimeService.getHomePage() }
    ) { homePageBody ->
        val parseBody = Jsoup.parse(homePageBody).body()
        val homePageParse = parseBody.select("div[class$=owl-theme]")
        val userInfo = parseBody.select("div[id=user-modal-dp-wrapper]").first()
        var avatarUrl: String? = null
        var username: String? = null
        userInfo?.let {
            avatarUrl = userInfo.select("img").first()?.absUrl("src")
            username = userInfo.getElementById("user-modal-name")?.text()
        }

        val latestHanimeClass = homePageParse[0]
        val latestUploadClass = homePageParse[1]
        val hotHanimeMonthlyClass = homePageParse[2]
        val hanimeCurrentClass = homePageParse[4]
        val hanimeTheyWatchedClass = homePageParse[5]

        // for latest hanime
        val latestHanimeList = mutableListOf<HanimeInfoModel>()
        val latestHanimeItems = latestHanimeClass.select("div[class=home-rows-videos-div]")
        latestHanimeItems.forEach { latestHanimeItem ->
            val coverUrl = latestHanimeItem.select("img")[0].absUrl("src")
            val title = latestHanimeItem.select("div[class$=title]").text()
            val redirectLink = latestHanimeItem.parent()!!.absUrl("href")
            latestHanimeList.add(
                HanimeInfoModel(
                    coverUrl = coverUrl,
                    title = title,
                    redirectLink = redirectLink,
                    itemType = HanimeInfoModel.SIMPLIFIED
                )
            )
        }

        // for latest upload
        val latestUploadList = mutableListOf<HanimeInfoModel>()
        val latestUploadItems = latestUploadClass.select("div[class$=card-mobile-panel]")
        latestUploadItems.forEach { latestUploadItem ->
            val title =
                latestUploadItem.select("div[class=card-mobile-title]")[0].text()
            val coverUrl =
                latestUploadItem.select("img")[1].absUrl("src")
            val redirectLink =
                latestUploadItem.select("a")[0].absUrl("href")
            val duration =
                latestUploadItem.select("div[class=card-mobile-duration]").text()
                    .trim()
            val uploader =
                latestUploadItem.select("div[class=card-mobile-user]").text()
            val views =
                latestUploadItem.select("span[class=card-mobile-views-text]").text()
            val uploadTime =
                latestUploadItem.select("span[class=card-mobile-created-text]").text()
            val genre =
                latestUploadItem.select("span[class=card-mobile-genre-new]").text()
            latestUploadList.add(
                HanimeInfoModel(
                    title = title,
                    coverUrl = coverUrl,
                    redirectLink = redirectLink,
                    duration = duration,
                    uploader = uploader,
                    views = views,
                    uploadTime = uploadTime,
                    genre = genre,
                    itemType = HanimeInfoModel.NORMAL
                )
            )
        }

        // for hot hanime monthly
        val hotHanimeMonthlyList = mutableListOf<HanimeInfoModel>()
        val hotHanimeMonthlyItems = hotHanimeMonthlyClass.children()
        hotHanimeMonthlyItems.forEach { hotHanimeMonthlyItem ->
            val coverUrl = hotHanimeMonthlyItem.select("img")[0].absUrl("src")
            val title = hotHanimeMonthlyItem.select("div[class$=title]").text()
            val redirectLink = hotHanimeMonthlyItem.select("a")[0].absUrl("href")
            hotHanimeMonthlyList.add(
                HanimeInfoModel(
                    coverUrl = coverUrl,
                    title = title,
                    redirectLink = redirectLink,
                    itemType = HanimeInfoModel.SIMPLIFIED
                )
            )
        }

        // for hanime current
        val hanimeCurrentList = mutableListOf<HanimeInfoModel>()
        val hanimeCurrentItems =
            hanimeCurrentClass.select("div[class=home-rows-videos-div]")
        hanimeCurrentItems.forEach { hanimeCurrentItem ->
            val coverUrl = hanimeCurrentItem.select("img")[0].absUrl("src")
            val title = hanimeCurrentItem.select("div[class$=title]").text()
            val redirectLink = hanimeCurrentItem.parent()!!.absUrl("href")
            hanimeCurrentList.add(
                HanimeInfoModel(
                    coverUrl = coverUrl,
                    title = title,
                    redirectLink = redirectLink,
                    itemType = HanimeInfoModel.SIMPLIFIED
                )
            )
        }

        // for hanime they watched
        val hanimeTheyWatchedList = mutableListOf<HanimeInfoModel>()
        val hanimeTheyWatchedItems =
            hanimeTheyWatchedClass.select("div[class$=card-mobile-panel]")
        hanimeTheyWatchedItems.forEach { hanimeTheyWatchedItem ->
            val title =
                hanimeTheyWatchedItem.select("div[class=card-mobile-title]")[0].text()
            val coverUrl =
                hanimeTheyWatchedItem.select("img")[1].absUrl("src")
            val redirectLink =
                hanimeTheyWatchedItem.select("a")[0].absUrl("href")
            val duration =
                hanimeTheyWatchedItem.select("div[class=card-mobile-duration]").text()
                    .trim()
            val uploader =
                hanimeTheyWatchedItem.select("div[class=card-mobile-user]").text()
            val views =
                hanimeTheyWatchedItem.select("span[class=card-mobile-views-text]").text()
            val uploadTime =
                hanimeTheyWatchedItem.select("span[class=card-mobile-created-text]").text()
            val genre =
                hanimeTheyWatchedItem.select("span[class=card-mobile-genre-new]").text()
            hanimeTheyWatchedList.add(
                HanimeInfoModel(
                    title = title,
                    coverUrl = coverUrl,
                    redirectLink = redirectLink,
                    duration = duration,
                    uploader = uploader,
                    views = views,
                    uploadTime = uploadTime,
                    genre = genre,
                    itemType = HanimeInfoModel.NORMAL
                )
            )
        }

        Log.d("late_hani_list", latestHanimeList.toString())
        Log.d("late_upload_list", latestUploadList.toString())
        Log.d("hot_hani_month_list", hotHanimeMonthlyList.toString())
        Log.d("hani_curr_list", hanimeCurrentList.toString())
        Log.d("hani_they_watch_list", hanimeTheyWatchedList.toString())

        // emit!
        return@websiteIOFlow WebsiteState.Success(
            HomePageModel(
                avatarUrl, username,
                latestHanimeList, latestUploadList, hotHanimeMonthlyList,
                hanimeCurrentList, hanimeTheyWatchedList
            )
        )
    }

    fun getHanimeSearchTags() = websiteIOFlow(
        request = { HanimeNetwork.hanimeService.getHanimeSearchResult() }
    ) { searchTagsBody ->
        val parseBody = Jsoup.parse(searchTagsBody).body()

        // for genres
        val genresList = mutableListOf<String>()
        val genresItems = parseBody.select("div[class~=genre-option]")
        genresItems.forEach { genresItem ->
            genresList.add(genresItem.text())
        }

        // for tags
        val tagsMap = linkedMapOf<String, List<String>>()
        val tagsClass = parseBody.getElementById("tags")!!
        val tagItems = tagsClass.select("div[class=modal-body]")[0].children()
        var tagType: String
        val tags = mutableListOf<String>()
        tagItems.forEach { tagItem ->
            if (tagItem.`is`("h5")) {
                tagType = tagItem.text().substringBefore("：")
                if (tags.isNotEmpty()) tagsMap[tagType] = tags
                tags.clear()
            } else if (tagItem.`is`("label")) {
                val tagName = tagItem.select("span")[0]
                tags.add(tagName.text())
            }
        }

        // for sort options
        val sortOptionsList = mutableListOf<String>()
        val sortOptionsItems = parseBody.select("div[class=hentai-sort-options]")
        sortOptionsItems.forEach { sortOptionsItem ->
            sortOptionsList.add(sortOptionsItem.text())
        }

        // for brands
        val brandsList = mutableListOf<String>()
        val brandsClass = parseBody.getElementById("brands")!!
        val brandsItems = brandsClass.select("label[class=hentai-tags-wrapper] > span")
        brandsItems.forEach { brandsItem ->
            brandsList.add(brandsItem.text())
        }

        // for release date
        val yearsList = mutableListOf<Pair<String, String>>()
        val monthsList = mutableListOf<Pair<String, String>>()
        val yearsClass = parseBody.select("select[id=year]")[0]
        val monthsClass = parseBody.select("select[id=month]")[0]
        val yearOptions = yearsClass.select("option")
        val monthOptions = monthsClass.select("option")
        yearOptions.forEach { yearOption ->
            val yearKey = yearOption.text()
            val yearValue = yearOption.attr("value")
            yearsList.add(yearKey to yearValue)
        }
        monthOptions.forEach { monthOption ->
            val monthKey = monthOption.text()
            val monthValue = monthOption.attr("value")
            monthsList.add(monthKey to monthValue)
        }

        // for duration options
        val durationOptionsList = mutableListOf(
            "全部" to EMPTY_STRING, "短片" to "（4 分鐘內）",
            "中長片" to "（4 至 20 分鐘）", "長片" to "（20 分鐘以上）"
        )

        // emit!
        return@websiteIOFlow WebsiteState.Success(
            SearchTagModel(
                genres = genresList, tags = tagsMap,
                sortOptions = sortOptionsList, brands = brandsList,
                releaseDates = SearchTagModel.ReleaseDate(
                    years = yearsList, months = monthsList
                ), durationOptions = durationOptionsList
            )
        )
    }

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
        }
    ) { resultBody ->
        val parseBody = Jsoup.parse(resultBody).body()
        val allContentsClass =
            parseBody.getElementsByClass("content-padding-new").first()
        val allSimplifiedContentsClass =
            parseBody.getElementsByClass("home-rows-videos-wrapper").first()

        val hanimeSearchList = mutableListOf<HanimeInfoModel>()

        if (allContentsClass != null) {
            val hanimeSearchItems =
                allContentsClass.select("div[class$=card-mobile-panel]")
            if (hanimeSearchItems.isEmpty()) {
                return@pageIOFlow PageLoadingState.NoMoreData()
            } else hanimeSearchItems.forEach { hanimeSearchItem ->
                val title =
                    hanimeSearchItem.select("div[class=card-mobile-title]")[0].text()
                val coverUrl =
                    hanimeSearchItem.select("img")[1].absUrl("src")
                val redirectLink =
                    hanimeSearchItem.select("a")[0].absUrl("href")
                val mDuration =
                    hanimeSearchItem.select("div[class=card-mobile-duration]").text()
                        .trim()
                val uploader =
                    hanimeSearchItem.select("div[class=card-mobile-user]").text()
                val views =
                    hanimeSearchItem.select("span[class=card-mobile-views-text]").text()
                val uploadTime =
                    hanimeSearchItem.select("span[class=card-mobile-created-text]").text()
                val mGenre =
                    hanimeSearchItem.select("span[class=card-mobile-genre-new]").text()
                hanimeSearchList.add(
                    HanimeInfoModel(
                        title = title,
                        coverUrl = coverUrl,
                        redirectLink = redirectLink,
                        duration = mDuration,
                        uploader = uploader,
                        views = views,
                        uploadTime = uploadTime,
                        genre = mGenre,
                        itemType = HanimeInfoModel.NORMAL
                    )
                )
            }
        } else if (allSimplifiedContentsClass != null) {
            val hanimeSearchItems = allSimplifiedContentsClass.children()
            if (hanimeSearchItems.isEmpty()) {
                return@pageIOFlow PageLoadingState.NoMoreData()
            } else hanimeSearchItems.forEach { hanimeSearchItem ->
                val redirectLink =
                    hanimeSearchItem.attr("href")
                val coverUrl =
                    hanimeSearchItem.select("img")[0].attr("src")
                val title =
                    hanimeSearchItem.select("div[class=home-rows-videos-title]")[0].text()
                hanimeSearchList.add(
                    HanimeInfoModel(
                        title = title,
                        coverUrl = coverUrl,
                        redirectLink = redirectLink,
                        itemType = HanimeInfoModel.SIMPLIFIED
                    )
                )
            }
        }

        // emit!
        return@pageIOFlow PageLoadingState.Success(hanimeSearchList)
    }

    fun getHanimeVideo(videoCode: String) = videoIOFlow(
        request = { HanimeNetwork.hanimeService.getHanimeVideo(videoCode) }
    ) { videoBody ->
        val parseBody = Jsoup.parse(videoBody).body()
        val csrfToken = parseBody.select("input[name=_token]").first()?.attr("value")
        val currentUserId = parseBody.select("input[name=like-user-id]").first()?.attr("value")

        val showPanel = parseBody.getElementsByClass("video-show-panel-width")[0]

        val title = showPanel.getElementById("shareBtn-title")!!.text()

        val introduction = showPanel.getElementById("caption")?.ownText()

        val uploadTimeWithViews = showPanel.select("p")[0].text()

        val tags = showPanel.getElementsByClass("single-video-tag")
        val tagList = mutableListOf<String>()
        tags.forEach { tag ->
            val child = tag.child(0)
            if (child.hasAttr("href")) {
                tagList.add(child.text())
            }
        }

        val playListWrapper = parseBody.select("div[id=video-playlist-wrapper]").first()
        var playList: HanimeVideoModel.PlayList? = null
        playListWrapper?.let {
            val playListVideoList = mutableListOf<HanimeInfoModel>()
            val playListName = it.select("div > h4")[0].text()
            val playListScroll = it.getElementById("playlist-scroll")!!
            playListScroll.children().forEach { parent ->
                val child = parent.child(0)
                val redirectUrl = child.absUrl("href")
                val eachTitleCover = child.select("div > img")[1]
                val eachIsPlaying = child.select("div > div").first() != null
                val eachViews = child.select("div > p").first()?.text()
                    ?.substringAfter('：')?.substringBefore('次')
                val playListEachCoverUrl = eachTitleCover.absUrl("src")
                val playListEachTitle = eachTitleCover.attr("alt")
                playListVideoList.add(
                    HanimeInfoModel(
                        title = playListEachTitle, coverUrl = playListEachCoverUrl,
                        redirectLink = redirectUrl, views = eachViews, isPlaying = eachIsPlaying,
                        itemType = HanimeInfoModel.NORMAL
                    )
                )
            }
            playList =
                HanimeVideoModel.PlayList(playListName = playListName, video = playListVideoList)
        }

        val relatedAnimeList = mutableListOf<HanimeInfoModel>()
        val relatedTabContent = parseBody.getElementById("related-tabcontent")
        relatedTabContent?.let {
            it.child(0).children().forEach { each ->
                val eachContent = each.select("a")[0]

                // simplified
                val homeRowsVideosDiv =
                    eachContent.getElementsByClass("home-rows-videos-div").first()

                if (homeRowsVideosDiv != null) {
                    val eachRedirect = eachContent.absUrl("href")
                    val eachCoverUrl = homeRowsVideosDiv.select("img")[0].absUrl("src")
                    val eachTitle = homeRowsVideosDiv.select("div[class$=title]")[0].text()
                    relatedAnimeList.add(
                        HanimeInfoModel(
                            title = eachTitle, coverUrl = eachCoverUrl,
                            redirectLink = eachRedirect,
                            itemType = HanimeInfoModel.SIMPLIFIED
                        )
                    )
                } else {

                    // normal
                    val previewWrapper = eachContent.select("div[class=preview-wrapper]")[0]
                    val cardInfoWrapper = eachContent.select("div[class=card-info-wrapper]")[0]

                    val eachRedirect = eachContent.absUrl("href")
                    val eachCoverUrl = previewWrapper.select("img")[0].absUrl("data-src")
                    val eachDuration = previewWrapper.select("div")[0].text()
                    val eachTitle = cardInfoWrapper.child(0).text()
                    val eachUploader = cardInfoWrapper.child(1).text()
                    val eachViewsAndUploadTime = cardInfoWrapper.child(2).text()
                    val eachViews = eachViewsAndUploadTime.substringAfter('：').substringBefore('次')
                    val eachUploadTime = eachViewsAndUploadTime.substringAfterLast(' ')
                    relatedAnimeList.add(
                        HanimeInfoModel(
                            title = eachTitle, coverUrl = eachCoverUrl,
                            redirectLink = eachRedirect, duration = eachDuration,
                            uploader = eachUploader, views = eachViews, uploadTime = eachUploadTime,
                            itemType = HanimeInfoModel.NORMAL
                        )
                    )
                }
            }
        }
        Log.d("related_anime_list", relatedAnimeList.toString())

        val videoUrlList = linkedMapOf<String, String>()
        val videoClass = parseBody.select("video[id=player]")[0]
        val videoCoverUrl = videoClass.absUrl("poster")
        videoClass.children().forEach { source ->
            if (source != null) {
                val resolution = source.attr("size") + "P"
                val sourceUrl = source.absUrl("src")
                videoUrlList[resolution] = sourceUrl
            } else return@forEach
        }

        return@videoIOFlow VideoLoadingState.Success(
            HanimeVideoModel(
                title = title, coverUrl = videoCoverUrl, uploadTimeWithViews = uploadTimeWithViews,
                introduction = introduction, videoUrls = videoUrlList,
                tags = tagList, playList = playList, relatedHanimes = relatedAnimeList,
                csrfToken = csrfToken, currentUserId = currentUserId
            )
        )
    }

    fun getHanimePreview(date: String) = websiteIOFlow(
        request = { HanimeNetwork.hanimeService.getHanimePreview(date) }
    ) { previewBody ->
        val parseBody = Jsoup.parse(previewBody).body()

        // latest hanime
        val latestHanimeList = mutableListOf<HanimeInfoModel>()
        val latestHanimeClass = parseBody.select("div[class$=owl-theme]").first()
        latestHanimeClass?.let {
            val latestHanimeItems = latestHanimeClass.select("div[class=home-rows-videos-div]")
            latestHanimeItems.forEach { latestHanimeItem ->
                val coverUrl = latestHanimeItem.select("img")[0].absUrl("src")
                val title = latestHanimeItem.select("div[class$=title]").text()
                val redirectLink = latestHanimeItem.parent()!!.absUrl("href")
                latestHanimeList.add(
                    HanimeInfoModel(
                        coverUrl = coverUrl,
                        title = title,
                        redirectLink = redirectLink /* empty string here! */,
                        itemType = HanimeInfoModel.SIMPLIFIED
                    )
                )
            }
        }

        val contentPaddingClass = parseBody.select("div[class=content-padding] > div")
        val previewInfo = mutableListOf<HanimePreviewModel.PreviewInfo>()
        for (i in 0 until contentPaddingClass.size / 2) {

            val firstPart = contentPaddingClass[i * 2]
            val secondPart = contentPaddingClass[i * 2 + 1]

            val videoCode = firstPart.id()
            val title = firstPart.select("h4")[0].text()
            val coverUrl = firstPart.select("div[class=preview-info-cover] > img")[0].absUrl("src")
            val previewInfoContentClass =
                firstPart.getElementsByClass("preview-info-content-padding")[0]
            val videoTitle = previewInfoContentClass.select("h4")[0].text()
            val brand = previewInfoContentClass.select("h5")[0].select("a")[0].text()
            val releaseDate = previewInfoContentClass.select("h5")[1].ownText()

            val introduction = secondPart.select("h5")[0].text()
            val tagClass = secondPart.select("div[class=single-video-tag] > a")
            val tags = mutableListOf<String>()
            tagClass.forEach {
                tags.add(it.text())
            }
            val relatedPicClass = secondPart.select("img[class=preview-image-modal-trigger]")
            val relatedPics = mutableListOf<String>()
            relatedPicClass.forEach {
                relatedPics.add(it.absUrl("src"))
            }

            previewInfo.add(
                HanimePreviewModel.PreviewInfo(
                    title = title,
                    videoTitle = videoTitle,
                    coverUrl = coverUrl,
                    introduction = introduction,
                    brand = brand,
                    releaseDate = releaseDate,
                    videoCode = videoCode,
                    tags = tags,
                    relatedPicsUrl = relatedPics
                )
            )
        }

        val header = parseBody.select("div[id=player-div-wrapper]")[0]
        val headerPicUrl = header.select("img")[0].absUrl("src")
        val hasPrevious = parseBody.getElementsByClass("hidden-md hidden-lg")[0]
            .select("div[style*=left]").first() != null
        val hasNext = parseBody.getElementsByClass("hidden-md hidden-lg")[0]
            .select("div[style*=right]").first() != null

        return@websiteIOFlow WebsiteState.Success(
            HanimePreviewModel(
                headerPicUrl = headerPicUrl,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                latestHanime = latestHanimeList,
                previewInfo = previewInfo
            )
        )
    }

    fun getMyList(page: Int, listType: MyListType) = pageIOFlow(
        request = { HanimeNetwork.hanimeService.getMyList(page, listType.value) }
    ) { listBody ->
        val parseBody = Jsoup.parse(listBody).body()
        val csrfToken = parseBody.select("input[name=_token]").first()?.attr("value")

        val myListHanimeList = mutableListOf<HanimeInfoModel>()
        val allHanimeClass = parseBody.getElementsByClass("home-rows-videos-wrapper").first()
        allHanimeClass?.let {
            if (allHanimeClass.childrenSize() == 0) {
                return@pageIOFlow PageLoadingState.NoMoreData()
            }
            allHanimeClass.children().forEach { videoElement ->
                val title =
                    videoElement.getElementsByClass("home-rows-videos-title")[0].text()
                val coverUrl =
                    videoElement.select("img").let {
                        if (it.size > 1) it[1] else it[0]
                    }.absUrl("src")
                val redirectLink =
                    videoElement.getElementsByClass("playlist-show-links")[0].absUrl("href")
                myListHanimeList.add(
                    HanimeInfoModel(
                        title = title, coverUrl = coverUrl,
                        redirectLink = redirectLink, itemType = HanimeInfoModel.NORMAL
                    )
                )
            }
        } ?: return@pageIOFlow PageLoadingState.Error(
            IllegalStateException("where is \"home-rows-videos-wrapper\" ???")
        )

        return@pageIOFlow PageLoadingState.Success(MyListModel(myListHanimeList, csrfToken))
    }

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

    fun getComment(type: String, code: String) = websiteIOFlow(
        request = { HanimeNetwork.commentService.getComment(type, code) }
    ) { commentRawBody ->
        val jsonObject = JSONObject(commentRawBody)
        val commentBody = jsonObject.get("comments").toString()
        val parseBody = Jsoup.parse(commentBody).body()
        val csrfToken = parseBody.select("input[name=_token]").first()?.attr("value")
        val currentUserId = parseBody.select("input[name=comment-user-id]").first()?.attr("value")
        val commentList = mutableListOf<VideoCommentModel.VideoComment>()
        val allCommentsClass = parseBody.getElementById("comment-start")
        allCommentsClass?.let {
            val avatarClasses = it.select("a > img")
            val contentClasses = it.getElementsByClass("comment-index-text") // 偶數是日期和作者，奇數是内容
            val replyClasses = it.select("div[id=comment-like-form-wrapper]")
            for (i in replyClasses.indices) {
                val avatarUrl = avatarClasses[i * 2].absUrl("src")
                val replyClass = replyClasses[i].select("div > div")
                val thumbUp = try {
                    val up = replyClass[0].child(1).text()
                    if (up.isInt()) {
                        up
                    } else {
                        throw IndexOutOfBoundsException("應該throw NumberFormatException，但這樣能統一catch")
                    }
                } catch (e: IndexOutOfBoundsException) {
                    replyClasses[i].select("span[class=comment-like-btn-wrapper] > button > span")[1].text()
                }
                val hasMoreReplies =
                    replyClasses[i].select("div[class~=load-replies-btn]").first() != null
                val id = replyClasses[i].select("div[id~=reply-section-wrapper]")[0].id()
                    .substringAfterLast('-')
                val usernameAndDateClass = contentClasses[i * 2]
                val username = usernameAndDateClass.select("a")[0].ownText()
                val date = usernameAndDateClass.select("a > span")[0].ownText()
                val content = contentClasses[i * 2 + 1].text()
                commentList.add(
                    VideoCommentModel.VideoComment(
                        avatar = avatarUrl, username = username, date = date,
                        content = content, thumbUp = thumbUp, hasMoreReplies = hasMoreReplies,
                        id = id, isChildComment = false
                    )
                )
            }
        }
        return@websiteIOFlow WebsiteState.Success(
            VideoCommentModel(
                commentList,
                currentUserId,
                csrfToken
            )
        )
    }

    fun getCommentReply(commentId: String) = websiteIOFlow(
        request = { HanimeNetwork.commentService.getCommentReply(commentId) }
    ) { replyRawBody ->
        val jsonObject = JSONObject(replyRawBody)
        val replyBody = jsonObject.get("replies").toString()
        val replyList = mutableListOf<VideoCommentModel.VideoComment>()
        val parseBody = Jsoup.parse(replyBody).body()
        val replyStart = parseBody.select("div[id*=reply-start]").first()
        replyStart?.let {
            val allRepliesClass = it.children()
            for (i in 0 until (allRepliesClass.size / 2)) {
                val avatarUrl = allRepliesClass[i * 2].select("img")[0].absUrl("src")
                val content =
                    allRepliesClass[i * 2].getElementsByClass("comment-index-text")[1].text()
                val usernameClass =
                    allRepliesClass[i * 2].getElementsByClass("comment-index-text")[0]
                val username = usernameClass.select("div > a")[0].ownText()
                val date = usernameClass.select("div > a > span")[0].ownText().trim()
                val thumbAndReplyClass = allRepliesClass[i * 2 + 1]
                val thumbUp = with(thumbAndReplyClass.select("span")) {
                    if (get(1).text().isInt()) get(1).text() else get(2).text()
                }

                replyList.add(
                    VideoCommentModel.VideoComment(
                        avatar = avatarUrl, username = username,
                        date = date, content = content, thumbUp = thumbUp,
                        isChildComment = true
                    )
                )
            }
        }

        return@websiteIOFlow WebsiteState.Success(VideoCommentModel(replyList))
    }

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
        emit(WebsiteState.Loading())
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
        emit(WebsiteState.Loading())
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
                emit(VideoLoadingState.NoContent())
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