package com.yenaly.yenaly_libs

import com.yenaly.yenaly_libs.utils.folderSize
import com.yenaly.yenaly_libs.utils.formatFileSizeV2
import com.yenaly.yenaly_libs.utils.md5
import com.yenaly.yenaly_libs.utils.secondToTimeCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UtilUnitTest {
    @Test
    fun formatFileSizeV2_isCorrect() {
        // Test with default parameters
        assertEquals("1 kB", 1000L.formatFileSizeV2(useSi = true))
        assertEquals("1 KiB", 1024L.formatFileSizeV2())

        // Test with different decimal places
        assertEquals(
            "1.00 kB",
            1000L.formatFileSizeV2(useSi = true, decimalPlaces = 2, stripTrailingZeros = false)
        )
        assertEquals(
            "1.00 KiB",
            1024L.formatFileSizeV2(decimalPlaces = 2, stripTrailingZeros = false)
        )

        // Test with stripTrailingZeros = false
        assertEquals("1.0 kB", 1000L.formatFileSizeV2(useSi = true, stripTrailingZeros = false))
        assertEquals("1.0 KiB", 1024L.formatFileSizeV2(stripTrailingZeros = false))

        // Test with different sizes
        assertEquals(
            "1.0 MB",
            1_000_000L.formatFileSizeV2(useSi = true, stripTrailingZeros = false)
        )
        assertEquals("1.0 MiB", 1_048_576L.formatFileSizeV2(stripTrailingZeros = false))
        assertEquals(
            "1.0 GB",
            1_000_000_000L.formatFileSizeV2(useSi = true, stripTrailingZeros = false)
        )
        assertEquals("1.0 GiB", 1_073_741_824L.formatFileSizeV2(stripTrailingZeros = false))

        // Test with edge cases
        assertEquals("999 B", 999L.formatFileSizeV2(useSi = true))
        assertEquals("1023 B", 1023L.formatFileSizeV2())
        assertEquals("1.0 kB", 1000L.formatFileSizeV2(useSi = true, stripTrailingZeros = false))
        assertEquals("1.0 KiB", 1024L.formatFileSizeV2(stripTrailingZeros = false))
    }

    @Test
    fun secondToTimeCase_isCorrect() {
        assertEquals("02:03", 123L.secondToTimeCase())
        assertEquals("00:09", 9L.secondToTimeCase())
        assertEquals("09:00", 540L.secondToTimeCase())
        assertEquals("01:00:00", 3600L.secondToTimeCase())
        assertEquals("00:00", 0L.secondToTimeCase())
        assertEquals("00:59", 59L.secondToTimeCase())
        assertEquals("01:01:01", 3661L.secondToTimeCase())
    }

    @Test
    fun md5_calculatesCorrectHashForNonEmptyFile() {
        val file = File.createTempFile("testFile", ".txt").apply {
            writeText("Hello, World!")
        }
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", file.md5())
        file.delete()
    }

    @Test
    fun md5_calculatesCorrectHashForEmptyFile() {
        val file = File.createTempFile("emptyFile", ".txt")
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", file.md5())
        file.delete()
    }

    @Test
    fun md5_calculatesCorrectHashForLargeFile() {
        val file = File.createTempFile("largeFile", ".txt").apply {
            writeText("a".repeat(10_000_000))
        }
        assertEquals("7095bae098259e0dda4b7acc624de4e2", file.md5())
        file.delete()
    }

    @Test
    fun folderSize_calculatesCorrectSizeForNonEmptyFolder() {
        val folder = File.createTempFile("testFolder", "").apply {
            delete()
            mkdir()
            File(this, "file1.txt").writeText("Hello")
            File(this, "file2.txt").writeText("World")
        }
        assertEquals(10L, folder.folderSize)
        folder.deleteRecursively()
    }

    @Test
    fun folderSize_calculatesCorrectSizeForEmptyFolder() {
        val folder = File.createTempFile("emptyFolder", "").apply {
            delete()
            mkdir()
        }
        assertEquals(0L, folder.folderSize)
        folder.deleteRecursively()
    }

    @Test
    fun folderSize_calculatesCorrectSizeForNestedFolders() {
        val folder = File.createTempFile("nestedFolder", "").apply {
            delete()
            mkdir()
            File(this, "file1.txt").writeText("Hello")
            File(this, "subFolder").apply {
                mkdir()
                File(this, "file2.txt").writeText("World")
            }
        }
        assertEquals(10L, folder.folderSize)
        folder.deleteRecursively()
    }

    @Test
    fun folderSize_returnsZeroForNullFile() {
        val folder: File? = null
        assertEquals(0L, folder.folderSize)
    }
}