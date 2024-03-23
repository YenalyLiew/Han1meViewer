package com.yenaly.han1meviewer.logic.network

import android.util.Log
import com.yenaly.han1meviewer.BuildConfig
import com.yenaly.han1meviewer.HA1_API_LATEST_RELEASE_URL
import com.yenaly.han1meviewer.HA1_GITHUB_API_URL
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.logic.model.github.Artifacts
import com.yenaly.han1meviewer.logic.model.github.CommitComparison
import com.yenaly.han1meviewer.logic.model.github.Latest
import com.yenaly.han1meviewer.logic.model.github.Release
import com.yenaly.han1meviewer.logic.model.github.WorkflowRuns
import com.yenaly.han1meviewer.util.await
import com.yenaly.han1meviewer.util.checkNeedUpdate
import com.yenaly.han1meviewer.util.copyTo
import com.yenaly.han1meviewer.util.parseAs
import com.yenaly.han1meviewer.util.runSuspendCatching
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.use
import org.json.JSONObject
import java.io.File
import java.net.InetAddress
import java.util.zip.ZipInputStream
import kotlin.time.Duration.Companion.days

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/21 021 08:28
 */
object HUpdater {

    private val updaterClient = OkHttpClient.Builder()
        .dns(UpdateDns)
        .build()

    /**
     * Check for update
     *
     * @param forceCheck force check
     */
    suspend fun checkForUpdate(forceCheck: Boolean = false): Latest? {
        val now = Clock.System.now()
        val lastCheckTime = Instant.fromEpochSeconds(Preferences.lastUpdatePopupTime)
        val interval = Preferences.updatePopupIntervalDays

        if (forceCheck || now > lastCheckTime + interval.days) {
            if (Preferences.useCIUpdateChannel) {
                val curSha = BuildConfig.COMMIT_SHA
                val apiReq = request(HA1_GITHUB_API_URL)
                val branch = apiReq.body?.string()?.let(::JSONObject)?.getString("default_branch")
                    ?: return null
                val workflowRunsUrl =
                    "$HA1_GITHUB_API_URL/actions/workflows/ci.yml/runs?branch=$branch&event=push&status=success&per_page=1"
                val workflowRun = request(workflowRunsUrl).parseAs<WorkflowRuns>()
                    ?.workflowRuns?.firstOrNull() ?: return null

                if (!forceCheck) Preferences.lastUpdatePopupTime = now.epochSeconds

                val shortSha = workflowRun.headSha.take(7)
                if (shortSha != curSha) {
                    val artifacts = request(workflowRun.artifactsUrl).parseAs<Artifacts>()
                        ?: return null
                    val archiveUrl = artifacts.downloadLink
                    val changelog = runSuspendCatching {
                        val commitCompUrl = "$HA1_GITHUB_API_URL/compare/$curSha...$shortSha"
                        val res = request(commitCompUrl).parseAs<CommitComparison>()
                        res?.commits?.joinToString("\n") { commit ->
                            "${commit.commit.message} (@${commit.commit.author.name})"
                        }
                    }.getOrNull() ?: workflowRun.title
                    return Latest("$shortSha (CI)", changelog, archiveUrl)
                }
            } else {
                val ver = request(HA1_API_LATEST_RELEASE_URL).parseAs<Release>() ?: return null
                val isNeeded = checkNeedUpdate(ver.tagName)

                if (!forceCheck) Preferences.lastUpdatePopupTime = now.epochSeconds

                if (isNeeded) {
                    return Latest(ver.tagName, ver.body, ver.assets.first().browserDownloadURL)
                }
            }
        }
        return null
    }

    /**
     * Inject update to file
     *
     * @param url update url
     */
    suspend fun File.injectUpdate(url: String, progress: ((Int) -> Unit)? = null) {
        val res = request(url)
        if (url.endsWith("zip")) {
            Log.d("HUpdater", "Injecting update from zip ($url)")
            res.body?.use { body ->
                body.byteStream().use { stream ->
                    ZipInputStream(stream).use { zip ->
                        zip.nextEntry
                        this.outputStream().use {
                            zip.copyTo(it, body.contentLength(), progress = progress)
                        }
                    }
                }
            }
        } else {
            Log.d("HUpdater", "Injecting update from release ($url)")
            this.outputStream().use {
                res.body?.use { body ->
                    body.byteStream().copyTo(it, body.contentLength(), progress = progress)
                }
            }
        }
    }

    /**
     * Request to url
     *
     * @param url request url
     */
    private suspend inline fun request(url: String): okhttp3.Response {
        val req = Request.Builder().url(url)
            .header("Authorization", "Bearer ${BuildConfig.HA1_GITHUB_TOKEN}")
            .build()
        return updaterClient.newCall(req).await()
    }

    object UpdateDns : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return when (hostname) {
                "api.github.com" -> listOf(InetAddress.getByName("140.82.121.6"))
                "github.com" -> listOf(InetAddress.getByName("140.82.121.4"))
                else -> Dns.SYSTEM.lookup(hostname)
            }
        }
    }
}