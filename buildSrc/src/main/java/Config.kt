@file:Suppress("UnstableApiUsage")

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

/**
 * @author Yenaly Liew
 * @time 2023/11/25 025 17:55
 */
object Config {

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
    }

    val Project.lastCommitSha: String
        get() = providers.exec {
            commandLine = "git rev-parse --short=7 HEAD".split(' ')
        }.standardOutput.asText.get().trim()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> Provider<String>.fetch(clazz: KClass<T>): T {
        return when (clazz) {
            Int::class -> get().toInt() as T
            Long::class -> get().toLong() as T
            Boolean::class -> get().toBoolean() as T
            String::class -> get() as T
            else -> error("Unsupported type: ${clazz.simpleName}")
        }
    }

    inline fun <reified T : Any> Provider<String>.fetch(): T = fetch(T::class)
}