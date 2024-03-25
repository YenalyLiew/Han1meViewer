import org.gradle.api.Project
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author Yenaly Liew
 * @time 2023/11/25 025 17:55
 */
object Config {

    const val compileSdk = 34
    const val minSdk = 24
    const val targetSdk = 34

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
}