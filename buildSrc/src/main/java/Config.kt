@file:Suppress("UnstableApiUsage")

import org.gradle.api.Project
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * @author Yenaly Liew
 * @time 2023/11/25 025 17:55
 */
object Config {

    val Project.isRelease: Boolean
        get() = gradle.startParameter.taskNames.any { it.contains("Release") }

    object Version {
        fun Int?.createVersionName(
            major: Int,
            minor: Int,
            patch: Int,
            isPreRelease: Boolean = false,
        ): String {
            val version = if (isPreRelease) {
                "${major}.${minor}.${patch}-pre+${this}"
            } else "${major}.${minor}.${patch}+${this}"
            return version.also { println("Version Name: $it") }
        }

        fun createVersionCode() = LocalDateTime.now(Clock.systemUTC()).format(
            DateTimeFormatter.ofPattern("yyMMddHH")
        ).toInt().also { println("Version Code: $it") }

        /**
         * 版本来源，用于区分不同的版本
         *
         * @return ci 或 official 或 debug
         */
        val Project.source: String
            get() = System.getenv("HA1_VERSION_SOURCE") ?: kotlin.run {
                return if (isRelease) "official" else "debug"
            }
    }

    val Project.lastCommitSha: String
        get() = providers.exec {
            commandLine = "git rev-parse --short=7 HEAD".split(' ')
        }.standardOutput.asText.get().trim()

    val thisYear: Int
        get() = LocalDateTime.now(Clock.system(ZoneId.of("UTC+8"))).year
}