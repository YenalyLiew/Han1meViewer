import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

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

        fun createVersionCode() = SimpleDateFormat("yyMMddHH").let {
            it.timeZone = TimeZone.getTimeZone("UTC")
            it.format(Date()).toInt()
        }.also { println("Version Code: $it") }
    }
}