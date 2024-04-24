package com.yenaly.han1meviewer.logic

import android.util.Log
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.HanimeResolution
import com.yenaly.han1meviewer.LOCAL_DATE_FORMAT
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.logic.exception.ParseException
import com.yenaly.han1meviewer.logic.model.HanimeInfo
import com.yenaly.han1meviewer.logic.model.HanimePreview
import com.yenaly.han1meviewer.logic.model.HanimeVideo
import com.yenaly.han1meviewer.logic.model.HomePage
import com.yenaly.han1meviewer.logic.model.MyListItems
import com.yenaly.han1meviewer.logic.model.Playlists
import com.yenaly.han1meviewer.logic.model.SearchTag
import com.yenaly.han1meviewer.logic.model.VideoComments
import com.yenaly.han1meviewer.logic.state.PageLoadingState
import com.yenaly.han1meviewer.logic.state.VideoLoadingState
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.toVideoCode
import kotlinx.datetime.LocalDate
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/07/31 031 16:43
 */
object Parse {

    /**
     * 所需 Regex
     */
    object Regex {
        val videoSource = Regex("""const source = '(.+)'""")
        val viewAndUploadTime = Regex("""觀看次數：(.+)次 *(\d{4}-\d{2}-\d{2})""")
    }

    fun extractTokenFromLoginPage(body: String): String {
        val parseBody = Jsoup.parse(body).body()
        return parseBody.selectFirst("input[name=_token]")?.attr("value")
            ?: throw ParseException("Can't find csrf token from login page.")
    }

    fun homePageVer2(body: String): WebsiteState<HomePage> {
        val parseBody = Jsoup.parse(body).body()
        val homePageParse = parseBody.select("div[id=home-rows-wrapper] > div")
        val userInfo = parseBody.selectFirst("div[id=user-modal-dp-wrapper]")
        val avatarUrl: String? = userInfo?.selectFirst("img")?.absUrl("src")
        val username: String? = userInfo?.getElementById("user-modal-name")?.text()

        val bannerCSS = parseBody.selectFirst("div[id=home-banner-wrapper]")
        val bannerImg = bannerCSS?.previousElementSibling()
        val bannerTitle = bannerImg?.selectFirst("img")?.attr("alt")
            .logIfParseNull(Parse::homePageVer2.name, "bannerTitle")
        val bannerPic = bannerImg?.select("img")?.getOrNull(1)?.absUrl("src")
            .logIfParseNull(Parse::homePageVer2.name, "bannerPic")
        val bannerDesc = bannerCSS?.selectFirst("h4")?.ownText()
        val bannerVideoCode =
            bannerCSS?.selectFirst("a[class~=play-btn]")?.absUrl("href")?.toVideoCode()
                .logIfParseNull(Parse::homePageVer2.name, "bannerVideoCode")
        val banner = if (bannerTitle != null && bannerPic != null && bannerVideoCode != null) {
            HomePage.Banner(
                title = bannerTitle, description = bannerDesc,
                picUrl = bannerPic, videoCode = bannerVideoCode,
            )
        } else null

        val latestHanimeClass = homePageParse.getOrNull(0)
        val latestReleaseClass = homePageParse.getOrNull(1)
        val latestUploadClass = homePageParse.getOrNull(2)
        val chineseSubtitleClass = homePageParse.getOrNull(3)
        val hotHanimeMonthlyClass = homePageParse.getOrNull(homePageParse.size - 2)
        val hanimeCurrentClass = homePageParse.getOrNull(homePageParse.size - 3)
        val hanimeTheyWatchedClass = homePageParse.getOrNull(4)

        // for latest hanime
        val latestHanimeList = mutableListOf<HanimeInfo>()
        val latestHanimeItems = latestHanimeClass?.select("div[class=home-rows-videos-div]")
        latestHanimeItems?.forEach { latestHanimeItem ->
            val coverUrl = latestHanimeItem.selectFirst("img")?.absUrl("src")
                .throwIfParseNull(Parse::homePageVer2.name, "coverUrl")
            val title = latestHanimeItem.selectFirst("div[class$=title]")?.text()
                .throwIfParseNull(Parse::homePageVer2.name, "title")
            val videoCode = latestHanimeItem.parent()?.absUrl("href")?.toVideoCode()
                .throwIfParseNull(Parse::homePageVer2.name, "videoCode")
            latestHanimeList.add(
                HanimeInfo(
                    coverUrl = coverUrl,
                    title = title,
                    videoCode = videoCode,
                    itemType = HanimeInfo.SIMPLIFIED
                )
            )
        }

        // for latest release
        val latestReleaseList = mutableListOf<HanimeInfo>()
        val latestReleaseItems = latestReleaseClass?.select("div[class^=card-mobile-panel]")
        latestReleaseItems?.forEachStep2 { latestReleaseItem ->
            hanimeNormalItemVer2(latestReleaseItem)?.let(latestReleaseList::add)
        }

        // for latest upload
        val latestUploadList = mutableListOf<HanimeInfo>()
        val latestUploadItems = latestUploadClass?.select("div[class^=card-mobile-panel]")
        latestUploadItems?.forEachStep2 { latestUploadItem ->
            hanimeNormalItemVer2(latestUploadItem)?.let(latestUploadList::add)
        }

        // for chinese subtitle
        val chineseSubtitleList = mutableListOf<HanimeInfo>()
        val chineseSubtitleItems = chineseSubtitleClass?.select("div[class^=card-mobile-panel]")
        chineseSubtitleItems?.forEachStep2 { chineseSubtitleItem ->
            hanimeNormalItemVer2(chineseSubtitleItem)?.let(chineseSubtitleList::add)
        }

        // for hanime they watched
        val hanimeTheyWatchedList = mutableListOf<HanimeInfo>()
        val hanimeTheyWatchedItems =
            hanimeTheyWatchedClass?.select("div[class^=card-mobile-panel]")
        hanimeTheyWatchedItems?.forEachStep2 { hanimeTheyWatchedItem ->
            hanimeNormalItemVer2(hanimeTheyWatchedItem)?.let(hanimeTheyWatchedList::add)
        }

        // for hanime current
        val hanimeCurrentList = mutableListOf<HanimeInfo>()
        val hanimeCurrentItems =
            hanimeCurrentClass?.select("div[class^=card-mobile-panel]")
        hanimeCurrentItems?.forEachStep2 { hanimeCurrentItem ->
            hanimeNormalItemVer2(hanimeCurrentItem)?.let(hanimeCurrentList::add)
        }

        // for hot hanime monthly
        val hotHanimeMonthlyList = mutableListOf<HanimeInfo>()
        val hotHanimeMonthlyItems =
            hotHanimeMonthlyClass?.select("div[class^=card-mobile-panel]")
        hotHanimeMonthlyItems?.forEachStep2 { hotHanimeMonthlyItem ->
            hanimeNormalItemVer2(hotHanimeMonthlyItem)?.let(hotHanimeMonthlyList::add)
        }

        // emit!
        return WebsiteState.Success(
            HomePage(
                avatarUrl, username, banner = banner,
                latestHanime = latestHanimeList,
                latestRelease = latestReleaseList,
                latestUpload = latestUploadList,
                chineseSubtitle = chineseSubtitleList,
                hanimeTheyWatched = hanimeTheyWatchedList,
                hanimeCurrent = hanimeCurrentList,
                hotHanimeMonthly = hotHanimeMonthlyList,
            )
        )
    }

    @Deprecated("暂时没啥用")
    fun hanimeSearchTags(body: String): WebsiteState<SearchTag> {
        val parseBody = Jsoup.parse(body).body()

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
        return WebsiteState.Success(
            SearchTag(
                genres = genresList, tags = tagsMap,
                sortOptions = sortOptionsList, brands = brandsList,
                releaseDates = SearchTag.ReleaseDate(
                    years = yearsList, months = monthsList
                ), durationOptions = durationOptionsList
            )
        )
    }

    fun hanimeSearch(body: String): PageLoadingState<MutableList<HanimeInfo>> {
        val parseBody = Jsoup.parse(body).body()
        val allContentsClass =
            parseBody.getElementsByClass("content-padding-new").firstOrNull()
        val allSimplifiedContentsClass =
            parseBody.getElementsByClass("home-rows-videos-wrapper").firstOrNull()

        // emit!
        if (allContentsClass != null) {
            return hanimeSearchNormalVer2(allContentsClass)
        } else if (allSimplifiedContentsClass != null) {
            return hanimeSearchSimplified(allSimplifiedContentsClass)
        }
        return PageLoadingState.Success(mutableListOf())
    }

    // 每一个正常视频单元
    // #issue-38: 解析錯誤，原來是加廣告了！所以遇到無法處理的直接返回null。
    private fun hanimeNormalItemVer2(hanimeSearchItem: Element): HanimeInfo? {
        val title =
            hanimeSearchItem.selectFirst("div[class=card-mobile-title]")?.text()
                .logIfParseNull(Parse::hanimeNormalItemVer2.name, "title") // title
        val coverUrl =
            hanimeSearchItem.select("img").getOrNull(1)?.absUrl("src")
                .logIfParseNull(Parse::hanimeNormalItemVer2.name, "coverUrl") // coverUrl
        val videoCode =
            hanimeSearchItem.previousElementSibling()?.absUrl("href")?.toVideoCode()
                .logIfParseNull(Parse::hanimeNormalItemVer2.name, "videoCode") // videoCode
        if (title == null || coverUrl == null || videoCode == null) return null
        val durationAndViews = hanimeSearchItem.select("div[class=card-mobile-duration]")
        val mDuration = durationAndViews.getOrNull(0)?.text() // 改了
        val views = durationAndViews.getOrNull(1)?.text() // 改了
        return HanimeInfo(
            title = title,
            coverUrl = coverUrl,
            videoCode = videoCode,
            duration = mDuration.logIfParseNull(Parse::hanimeNormalItemVer2.name, "duration"),
            uploader = null,
            views = views.logIfParseNull(Parse::hanimeNormalItemVer2.name, "views"),
            uploadTime = null,
            genre = null,
            itemType = HanimeInfo.NORMAL
        )
    }

    // 每一个简化版视频单元
    private fun hanimeSimplifiedItem(hanimeSearchItem: Element): HanimeInfo? {
        val videoCode = hanimeSearchItem.attr("href").toVideoCode()
            .logIfParseNull(Parse::hanimeSimplifiedItem.name, "videoCode")
        val coverUrl = hanimeSearchItem.selectFirst("img")?.attr("src")
            .logIfParseNull(Parse::hanimeSimplifiedItem.name, "coverUrl")
        val title = hanimeSearchItem.selectFirst("div[class=home-rows-videos-title]")?.text()
            .logIfParseNull(Parse::hanimeSimplifiedItem.name, "title")
        if (videoCode == null || coverUrl == null || title == null) return null
        return HanimeInfo(
            title = title,
            coverUrl = coverUrl,
            videoCode = videoCode,
            itemType = HanimeInfo.SIMPLIFIED
        )
    }

    // 出来后是正常视频单元的页面用这个
    private fun hanimeSearchNormalVer2(
        allContentsClass: Element,
    ): PageLoadingState<MutableList<HanimeInfo>> {
        val hanimeSearchList = mutableListOf<HanimeInfo>()
        val hanimeSearchItems =
            allContentsClass.select("div[class^=card-mobile-panel]")
        if (hanimeSearchItems.isEmpty()) {
            return PageLoadingState.NoMoreData
        } else {
            hanimeSearchItems.forEachStep2 { hanimeSearchItem ->
                hanimeNormalItemVer2(hanimeSearchItem)?.let(hanimeSearchList::add)
            }
        }
        Log.d("search_result", "$hanimeSearchList")
        return PageLoadingState.Success(hanimeSearchList)
    }

    // 出来后是简化版视频单元的页面用这个
    private fun hanimeSearchSimplified(
        allSimplifiedContentsClass: Element,
    ): PageLoadingState<MutableList<HanimeInfo>> {
        val hanimeSearchList = mutableListOf<HanimeInfo>()
        val hanimeSearchItems = allSimplifiedContentsClass.children()
        if (hanimeSearchItems.isEmpty()) {
            return PageLoadingState.NoMoreData
        } else hanimeSearchItems.forEach { hanimeSearchItem ->
            hanimeSimplifiedItem(hanimeSearchItem)?.let(hanimeSearchList::add)
        }
        return PageLoadingState.Success(hanimeSearchList)
    }

    fun hanimeVideoVer2(body: String): VideoLoadingState<HanimeVideo> {
        val parseBody = Jsoup.parse(body).body()
        val csrfToken = parseBody.selectFirst("input[name=_token]")?.attr("value") // csrf token

        val currentUserId =
            parseBody.selectFirst("input[name=like-user-id]")?.attr("value") // current user id

        val title = parseBody.getElementById("shareBtn-title")?.text()
            .throwIfParseNull(Parse::hanimeVideoVer2.name, "title")

        val likeStatus = parseBody.selectFirst("input[name=like-status]")
            ?.attr("value")
        val likesCount = parseBody.selectFirst("input[name=likes-count]")
            ?.attr("value")?.toIntOrNull()

        val videoDetailWrapper = parseBody.selectFirst("div[class=video-details-wrapper]")
        val videoCaptionText = videoDetailWrapper?.selectFirst("div[class^=video-caption-text]")
        val chineseTitle = videoCaptionText?.previousElementSibling()?.ownText()
        val introduction = videoCaptionText?.ownText()
        val uploadTimeWithViews = videoDetailWrapper?.selectFirst("div > div > div")?.text()
        val uploadTimeWithViewsGroups = uploadTimeWithViews?.let {
            Regex.viewAndUploadTime.find(it)?.groups
        }
        val uploadTime = uploadTimeWithViewsGroups?.get(2)?.value?.let { time ->
            LocalDate.parse(time, LOCAL_DATE_FORMAT)
        }

        val views = uploadTimeWithViewsGroups?.get(1)?.value

        val tags = parseBody.getElementsByClass("single-video-tag")
        val tagList = mutableListOf<String>()
        tags.forEach { tag ->
            val child = tag.childOrNull(0)
            if (child != null && child.hasAttr("href")) {
                tagList.add(child.text())
            }
        }

        val myListCheckboxWrapper = parseBody.select("div[class~=playlist-checkbox-wrapper]")
        val myListInfo = mutableListOf<HanimeVideo.MyList.MyListInfo>()
        myListCheckboxWrapper.forEach {
            val listTitle = it.selectFirst("span")?.ownText()
                .logIfParseNull(Parse::hanimeVideoVer2.name, "myListTitle", loginNeeded = true)
            val listInput = it.selectFirst("input")
            val listCode = listInput?.attr("id")
                .logIfParseNull(Parse::hanimeVideoVer2.name, "myListCode", loginNeeded = true)
            val isSelected = listInput?.hasAttr("checked") ?: false
            if (listTitle != null && listCode != null) {
                myListInfo += HanimeVideo.MyList.MyListInfo(
                    code = listCode, title = listTitle, isSelected = isSelected
                )
            }
        }
        val isWatchLater = parseBody.getElementById("playlist-save-checkbox")
            ?.selectFirst("input")?.hasAttr("checked") ?: false
        val myList = HanimeVideo.MyList(isWatchLater = isWatchLater, myListInfo = myListInfo)

        val playlistWrapper = parseBody.selectFirst("div[id=video-playlist-wrapper]")
        val playlist = playlistWrapper?.let {
            val playlistVideoList = mutableListOf<HanimeInfo>()
            val playlistName = it.selectFirst("div > div > h4")?.text()
            val playlistScroll = it.getElementById("playlist-scroll")
            playlistScroll?.children()?.forEach { parent ->
                val videoCode = parent.selectFirst("div > a")?.absUrl("href")?.toVideoCode()
                    .throwIfParseNull(Parse::hanimeVideoVer2.name, "videoCode")
                val cardMobilePanel = parent.selectFirst("div[class^=card-mobile-panel]")
                val eachTitleCover = cardMobilePanel?.select("div > div > div > img")?.getOrNull(1)
                val eachIsPlaying = cardMobilePanel?.select("div > div > div > div")
                    ?.firstOrNull()
                    ?.text()
                    ?.contains("播放") ?: false
                val cardMobileDuration = cardMobilePanel?.select("div[class=card-mobile-duration]")
                val eachDuration = cardMobileDuration?.firstOrNull()?.text()
                val eachViews = cardMobileDuration?.getOrNull(1)?.text()
                    ?.substringBefore("次")
                val playlistEachCoverUrl = eachTitleCover?.absUrl("src")
                    .throwIfParseNull(Parse::hanimeVideoVer2.name, "playlistEachCoverUrl")
                val playlistEachTitle = eachTitleCover?.attr("alt")
                    .throwIfParseNull(Parse::hanimeVideoVer2.name, "playlistEachTitle")
                playlistVideoList.add(
                    HanimeInfo(
                        title = playlistEachTitle, coverUrl = playlistEachCoverUrl,
                        videoCode = videoCode,
                        duration = eachDuration.logIfParseNull(
                            Parse::hanimeVideoVer2.name,
                            "$playlistEachTitle duration"
                        ),
                        views = eachViews.logIfParseNull(
                            Parse::hanimeVideoVer2.name,
                            "$playlistEachTitle views"
                        ),
                        isPlaying = eachIsPlaying,
                        itemType = HanimeInfo.NORMAL
                    )
                )
            }
            HanimeVideo.Playlist(playlistName = playlistName, video = playlistVideoList)
        }

        val relatedAnimeList = mutableListOf<HanimeInfo>()
        val relatedTabContent = parseBody.getElementById("related-tabcontent")

        relatedTabContent?.also {
            val children = it.childOrNull(0)?.children()
            val isSimplified =
                children?.getOrNull(0)?.select("a")?.getOrNull(0)
                    ?.getElementsByClass("home-rows-videos-div")
                    ?.firstOrNull() != null
            if (isSimplified) {
                if (children != null) {
                    for (each in children) {
                        val eachContent = each.selectFirst("a")
                        val homeRowsVideosDiv =
                            eachContent?.getElementsByClass("home-rows-videos-div")?.firstOrNull()

                        if (homeRowsVideosDiv != null) {
                            val eachVideoCode = eachContent.absUrl("href").toVideoCode() ?: continue
                            val eachCoverUrl = homeRowsVideosDiv.selectFirst("img")?.absUrl("src")
                                .throwIfParseNull(Parse::hanimeVideoVer2.name, "eachCoverUrl")
                            val eachTitle =
                                homeRowsVideosDiv.selectFirst("div[class$=title]")?.text()
                                    .throwIfParseNull(Parse::hanimeVideoVer2.name, "eachTitle")
                            relatedAnimeList.add(
                                HanimeInfo(
                                    title = eachTitle, coverUrl = eachCoverUrl,
                                    videoCode = eachVideoCode,
                                    itemType = HanimeInfo.SIMPLIFIED
                                )
                            )
                        }
                    }
                }
            } else {
                children?.forEachStep2 { each ->
                    val item = each.select("div[class^=card-mobile-panel]")[0]
                    hanimeNormalItemVer2(item)?.let(relatedAnimeList::add)
                }
            }
        }
        Log.d("related_anime_list", relatedAnimeList.toString())

        val hanimeResolution = HanimeResolution()
        val videoClass = parseBody.selectFirst("video[id=player]")
        val videoCoverUrl = videoClass?.absUrl("poster").orEmpty()
        val videos = videoClass?.children()
        if (!videos.isNullOrEmpty()) {
            videos.forEach { source ->
                val resolution = source.attr("size") + "P"
                val sourceUrl = source.absUrl("src")
                val videoType = source.attr("type")
                hanimeResolution.parseResolution(resolution, sourceUrl, videoType)
            }
        } else {
            val playerDivWrapper = parseBody.selectFirst("div[id=player-div-wrapper]")
            playerDivWrapper?.select("script")?.let { scripts ->
                for (script in scripts) {
                    val data = script.data()
                    if (data.isBlank()) continue
                    val result =
                        Regex.videoSource.find(data)?.groups?.get(1)?.value ?: continue
                    hanimeResolution.parseResolution(null, result)
                    break
                }
            }
        }

        val artistAvatarUrl = parseBody.getElementById("video-user-avatar")?.absUrl("src")
        val artistNameCSS = parseBody.getElementById("video-artist-name")
        val artistGenre = artistNameCSS?.nextElementSibling()?.text()?.trim()
        val artistName = artistNameCSS?.text()?.trim()
        val artist = if (artistAvatarUrl != null && artistName != null && artistGenre != null) {
            HanimeVideo.Artist(
                name = artistName,
                avatarUrl = artistAvatarUrl,
                genre = artistGenre,
            )
        } else null

        return VideoLoadingState.Success(
            HanimeVideo(
                title = title, coverUrl = videoCoverUrl,
                chineseTitle = chineseTitle.logIfParseNull(
                    Parse::hanimeVideoVer2.name,
                    "chineseTitle"
                ),
                uploadTime = uploadTime.logIfParseNull(Parse::hanimeVideoVer2.name, "uploadTime"),
                views = views.logIfParseNull(Parse::hanimeVideoVer2.name, "views"),
                introduction = introduction.logIfParseNull(
                    Parse::hanimeVideoVer2.name,
                    "introduction"
                ),
                videoUrls = hanimeResolution.toResolutionLinkMap(),
                tags = tagList,
                myList = myList,
                playlist = playlist,
                relatedHanimes = relatedAnimeList,
                artist = artist.logIfParseNull(Parse::hanimeVideoVer2.name, "artist"),
                favTimes = likesCount,
                isFav = likeStatus == "1",
                csrfToken = csrfToken,
                currentUserId = currentUserId
            )
        )
    }

    fun hanimePreview(body: String): WebsiteState<HanimePreview> {
        val parseBody = Jsoup.parse(body).body()

        // latest hanime
        val latestHanimeList = mutableListOf<HanimeInfo>()
        val latestHanimeClass = parseBody.selectFirst("div[class$=owl-theme]")
        latestHanimeClass?.let {
            val latestHanimeItems = latestHanimeClass.select("div[class=home-rows-videos-div]")
            latestHanimeItems.forEach { latestHanimeItem ->
                val coverUrl = latestHanimeItem.selectFirst("img")?.absUrl("src")
                    .throwIfParseNull(Parse::hanimePreview.name, "coverUrl")
                val title = latestHanimeItem.selectFirst("div[class$=title]")?.text()
                    .throwIfParseNull(Parse::hanimePreview.name, "title")
                latestHanimeList.add(
                    HanimeInfo(
                        coverUrl = coverUrl,
                        title = title,
                        videoCode = EMPTY_STRING /* empty string here! */,
                        itemType = HanimeInfo.SIMPLIFIED
                    )
                )
            }
        }

        val contentPaddingClass = parseBody.select("div[class=content-padding] > div")
        val previewInfo = mutableListOf<HanimePreview.PreviewInfo>()
        for (i in 0 until contentPaddingClass.size / 2) {

            val firstPart = contentPaddingClass.getOrNull(i * 2)
            val secondPart = contentPaddingClass.getOrNull(i * 2 + 1)

            val videoCode = firstPart?.id()
            val title = firstPart?.selectFirst("h4")?.text()
            val coverUrl =
                firstPart?.selectFirst("div[class=preview-info-cover] > img")?.absUrl("src")
            val previewInfoContentClass =
                firstPart?.getElementsByClass("preview-info-content-padding")?.firstOrNull()
            val videoTitle = previewInfoContentClass?.selectFirst("h4")?.text()
            val brand = previewInfoContentClass?.selectFirst("h5")?.selectFirst("a")?.text()
            val releaseDate = previewInfoContentClass?.select("h5")?.getOrNull(1)?.ownText()

            val introduction = secondPart?.selectFirst("h5")?.text()
            val tagClass = secondPart?.select("div[class=single-video-tag] > a")
            val tags = mutableListOf<String>()
            tagClass?.forEach { tag: Element? ->
                tag?.let { tags.add(tag.text()) }
            }
            val relatedPicClass = secondPart?.select("img[class=preview-image-modal-trigger]")
            val relatedPics = mutableListOf<String>()
            relatedPicClass?.forEach { relatedPic: Element? ->
                relatedPic?.let { relatedPics.add(relatedPic.absUrl("src")) }
            }

            previewInfo.add(
                HanimePreview.PreviewInfo(
                    title = title,
                    videoTitle = videoTitle,
                    coverUrl = coverUrl,
                    introduction = introduction.logIfParseNull(
                        Parse::hanimePreview.name,
                        "$title introduction"
                    ),
                    brand = brand.logIfParseNull(Parse::hanimePreview.name, "$title brand"),
                    releaseDate = releaseDate.logIfParseNull(
                        Parse::hanimePreview.name,
                        "$title releaseDate"
                    ),
                    videoCode = videoCode.logIfParseNull(
                        Parse::hanimePreview.name,
                        "$title videoCode"
                    ),
                    tags = tags,
                    relatedPicsUrl = relatedPics
                )
            )
        }

        val header = parseBody.selectFirst("div[id=player-div-wrapper]")
        val headerPicUrl = header?.selectFirst("img")?.absUrl("src")
        val hasPrevious = parseBody.getElementsByClass("hidden-md hidden-lg").firstOrNull()
            ?.select("div[style*=left]")?.firstOrNull() != null
        val hasNext = parseBody.getElementsByClass("hidden-md hidden-lg").firstOrNull()
            ?.select("div[style*=right]")?.firstOrNull() != null

        return WebsiteState.Success(
            HanimePreview(
                headerPicUrl = headerPicUrl.logIfParseNull(
                    Parse::hanimePreview.name,
                    "headerPicUrl"
                ),
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                latestHanime = latestHanimeList,
                previewInfo = previewInfo
            )
        )
    }

    fun myListItems(body: String, typeOrCode: Any): PageLoadingState<MyListItems> {
        val parseBody = Jsoup.parse(body).body()
        val csrfToken = parseBody.selectFirst("input[name=_token]")?.attr("value")
        val desc = parseBody.getElementById("playlist-show-description")?.ownText()

        val myListHanimeList = mutableListOf<HanimeInfo>()
        val allHanimeClass = parseBody.getElementsByClass("home-rows-videos-wrapper").firstOrNull()
        allHanimeClass?.let {
            if (allHanimeClass.childrenSize() == 0) {
                return PageLoadingState.NoMoreData
            }
            allHanimeClass.children().forEach { videoElement ->
                val title =
                    videoElement.getElementsByClass("home-rows-videos-title")
                        .firstOrNull()?.text()
                        .throwIfParseNull(Parse::myListItems.name, "title")
                val coverUrl =
                    videoElement.select("img").let {
                        it.getOrNull(1) ?: it.firstOrNull()
                    }?.absUrl("src")
                        .throwIfParseNull(Parse::myListItems.name, "coverUrl")
                val videoCode =
                    videoElement.getElementsByClass("playlist-show-links")
                        .firstOrNull()?.absUrl("href")?.toVideoCode()
                        .throwIfParseNull(Parse::myListItems.name, "videoCode")
                myListHanimeList.add(
                    HanimeInfo(
                        title = title, coverUrl = coverUrl,
                        videoCode = videoCode, itemType = HanimeInfo.SIMPLIFIED
                    )
                )
            }
        }.logIfParseNull(Parse::myListItems.name, "allHanimeClass_CSS")

        return PageLoadingState.Success(
            MyListItems(
                myListHanimeList,
                typeOrCode = typeOrCode,
                desc = desc,
                csrfToken = csrfToken
            )
        )
    }

    fun playlists(body: String): WebsiteState<Playlists> {
        val parseBody = Jsoup.parse(body).body()
        val csrfToken = parseBody.selectFirst("input[name=_token]")?.attr("value")
        val lists = parseBody.select("div[class~=single-user-playlist]")
        val playlists = mutableListOf<Playlists.Playlist>()
        lists.forEach {
            val listCode = it.childOrNull(0)?.absUrl("href")?.substringAfter('=')
                .throwIfParseNull(Parse::playlists.name, "listCode")
            val listTitle = it.selectFirst("div[class=card-mobile-title]")?.ownText()
                .throwIfParseNull(Parse::playlists.name, "listTitle")
            val listTotal = it.selectFirst("div[style]")?.text()?.toIntOrNull()
                .throwIfParseNull(Parse::playlists.name, "listName")
            playlists += Playlists.Playlist(
                listCode = listCode, title = listTitle, total = listTotal
            )
        }
        return WebsiteState.Success(Playlists(playlists = playlists, csrfToken = csrfToken))
    }

    fun comments(body: String): WebsiteState<VideoComments> {
        val jsonObject = JSONObject(body)
        val commentBody = jsonObject.get("comments").toString()
        val parseBody = Jsoup.parse(commentBody).body()
        val csrfToken = parseBody.selectFirst("input[name=_token]")?.attr("value")
        val currentUserId = parseBody.selectFirst("input[name=comment-user-id]")?.attr("value")
        val commentList = mutableListOf<VideoComments.VideoComment>()
        val allCommentsClass = parseBody.getElementById("comment-start")
        allCommentsClass?.children()?.forEach { child: Element ->
            val avatarUrl = child.selectFirst("img")?.absUrl("src")
                .throwIfParseNull(Parse::comments.name, "avatarUrl")
            val textClass = child.getElementsByClass("comment-index-text")
            val nameAndDateClass = textClass.firstOrNull()
            val username = nameAndDateClass?.selectFirst("a")?.ownText()?.trim()
                .throwIfParseNull(Parse::comments.name, "name")
            val date = nameAndDateClass?.selectFirst("span")?.ownText()?.trim()
                .throwIfParseNull(Parse::comments.name, "date")
            val content = textClass.getOrNull(1)?.text()
                .throwIfParseNull(Parse::comments.name, "content")
            val hasMoreReplies = child.selectFirst("div[class^=load-replies-btn]") != null
            val thumbUp = child.getElementById("comment-like-form-wrapper")
                ?.select("span[style]")?.getOrNull(1)
                ?.text()?.toIntOrNull()
            val id = child.selectFirst("div[id^=reply-section-wrapper]")
                ?.id()?.substringAfterLast("-")

            val foreignId = child.getElementById("foreign_id")?.attr("value")
            val isPositive = child.getElementById("is_positive")?.attr("value")
            val likeUserId = child.selectFirst("input[name=comment-like-user-id]")?.attr("value")
            val commentLikesCount =
                child.selectFirst("input[name=comment-likes-count]")?.attr("value")
            val commentLikesSum = child.selectFirst("input[name=comment-likes-sum]")?.attr("value")
            val likeCommentStatus =
                child.selectFirst("input[name=like-comment-status]")?.attr("value")
            val unlikeCommentStatus =
                child.selectFirst("input[name=unlike-comment-status]")?.attr("value")

            val post = VideoComments.VideoComment.POST(
                foreignId.logIfParseNull(Parse::comments.name, "foreignId", loginNeeded = true),
                isPositive == "1",
                likeUserId.logIfParseNull(Parse::comments.name, "likeUserId", loginNeeded = true),
                commentLikesCount?.toIntOrNull().logIfParseNull(
                    Parse::comments.name,
                    "commentLikesCount", loginNeeded = true
                ),
                commentLikesSum?.toIntOrNull().logIfParseNull(
                    Parse::comments.name,
                    "commentLikesSum", loginNeeded = true
                ),
                likeCommentStatus == "1",
                unlikeCommentStatus == "1",
            )
            commentList.add(
                VideoComments.VideoComment(
                    avatar = avatarUrl, username = username, date = date,
                    content = content, hasMoreReplies = hasMoreReplies,
                    thumbUp = thumbUp.logIfParseNull(Parse::comments.name, "thumbUp"),
                    id = id.logIfParseNull(Parse::comments.name, "id"),
                    isChildComment = false, post = post
                )
            )
        }
        Log.d("commentList", commentList.toString())
        return WebsiteState.Success(
            VideoComments(
                commentList,
                currentUserId,
                csrfToken
            )
        )
    }

    fun commentReply(body: String): WebsiteState<VideoComments> {
        val jsonObject = JSONObject(body)
        val replyBody = jsonObject.get("replies").toString()
        val replyList = mutableListOf<VideoComments.VideoComment>()
        val parseBody = Jsoup.parse(replyBody).body()
        val replyStart = parseBody.selectFirst("div[id^=reply-start]")
        replyStart?.let {
            val allRepliesClass = it.children()
            for (i in allRepliesClass.indices step 2) {
                val basicClass = allRepliesClass.getOrNull(i)
                val postClass = allRepliesClass.getOrNull(i + 1)

                val avatarUrl = basicClass?.selectFirst("img")?.absUrl("src")
                    .throwIfParseNull(Parse::commentReply.name, "avatarUrl")
                val textClass = basicClass?.getElementsByClass("comment-index-text")
                val nameAndDateClass = textClass?.firstOrNull()
                val username = nameAndDateClass?.selectFirst("a")?.ownText()?.trim()
                    .throwIfParseNull(Parse::commentReply.name, "name")
                val date = nameAndDateClass?.selectFirst("span")?.ownText()?.trim()
                    .throwIfParseNull(Parse::commentReply.name, "date")
                val content = textClass?.getOrNull(1)?.text()
                    .throwIfParseNull(Parse::commentReply.name, "content")
                val thumbUp = postClass
                    ?.select("span[style]")?.getOrNull(1)
                    ?.text()?.toIntOrNull()

                val foreignId =
                    postClass?.getElementById("foreign_id")?.attr("value")
                val isPositive =
                    postClass?.getElementById("is_positive")?.attr("value")
                val likeUserId =
                    postClass?.selectFirst("input[name=comment-like-user-id]")?.attr("value")
                val commentLikesCount =
                    postClass?.selectFirst("input[name=comment-likes-count]")?.attr("value")
                val commentLikesSum =
                    postClass?.selectFirst("input[name=comment-likes-sum]")?.attr("value")
                val likeCommentStatus =
                    postClass?.selectFirst("input[name=like-comment-status]")?.attr("value")
                val unlikeCommentStatus =
                    postClass?.selectFirst("input[name=unlike-comment-status]")?.attr("value")
                val post = VideoComments.VideoComment.POST(
                    foreignId.logIfParseNull(
                        Parse::commentReply.name,
                        "foreignId",
                        loginNeeded = true
                    ),
                    isPositive == "1",
                    likeUserId.logIfParseNull(
                        Parse::commentReply.name,
                        "likeUserId",
                        loginNeeded = true
                    ),
                    commentLikesCount?.toIntOrNull().logIfParseNull(
                        Parse::commentReply.name,
                        "commentLikesCount", loginNeeded = true
                    ),
                    commentLikesSum?.toIntOrNull().logIfParseNull(
                        Parse::commentReply.name,
                        "commentLikesSum", loginNeeded = true
                    ),
                    likeCommentStatus == "1",
                    unlikeCommentStatus == "1",
                )
                replyList.add(
                    VideoComments.VideoComment(
                        avatar = avatarUrl, username = username, date = date,
                        content = content,
                        thumbUp = thumbUp.logIfParseNull(Parse::commentReply.name, "thumbUp"),
                        id = null,
                        isChildComment = true, post = post
                    )
                )
            }
        }

        return WebsiteState.Success(VideoComments(replyList))
    }

    /**
     * 這個網站的網頁結構真的很奇怪，所以我寫了一個 forEachStep2 來處理
     */
    private inline fun Elements.forEachStep2(action: (Element) -> Unit) {
        for (i in 0 until size step 2) {
            action(get(i))
        }
    }

    /**
     * 得到 Element 的 child，如果 index 超出範圍，就返回 null
     */
    private fun Element.childOrNull(index: Int): Element? {
        return try {
            child(index)
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    /**
     * 基本都是必需的參數，所以如果是 null，就直接丟出 [ParseException]
     *
     * @param funcName 這個參數是在哪個函數中被使用的
     * @param varName 這個參數的名稱
     * @return 如果 [this] 不是 null，就回傳 [this]
     * @throws ParseException 如果 [this] 是 null，就丟出 [ParseException]
     */
    private fun <T> T?.throwIfParseNull(funcName: String, varName: String): T = this
        ?: throw ParseException(funcName, varName)

    /**
     * 如果 [this] 是 null，就在 logcat 中顯示訊息
     *
     * @param funcName 這個參數是在哪個函數中被使用的
     * @param varName 這個參數的名稱
     * @return 回傳 [this]
     */
    private fun <T> T?.logIfParseNull(
        funcName: String, varName: String, loginNeeded: Boolean = false,
    ): T? = also {
        if (it == null) {
            if (loginNeeded && isAlreadyLogin) {
                Log.d("Parse::$funcName", "[$varName] is null. 而且處於登入狀態，這有點不正常")
            } else {
                Log.d("Parse::$funcName", "[$varName] is null. 這有點不正常")
            }
        }
    }
}