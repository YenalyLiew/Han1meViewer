@file:Suppress("unused")
@file:JvmName("AesUtil")

package com.yenaly.yenaly_libs.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by luyao
 * on 2019/7/1 16:09
 */

private const val KEY_ALGORITHM = "AES"
private const val CIPHER_ALGORITHM_DEFAULT = "AES"
const val AES_CFB_NOPADDING = "AES/CFB/NoPadding"
const val AES_ECB_NOPADDING = "AES/ECB/NoPadding"

/**
 * Aes encrypt byte array
 * @param key the encryption key
 * @param iv the IV (CFB,CBC,CTR need IV)
 * @param algorithm the algorithm parameters
 */
fun ByteArray.aesEncrypt(
    key: ByteArray,
    iv: ByteArray = ByteArray(16),
    algorithm: String = AES_CFB_NOPADDING
): ByteArray {
    val cipher = initCipher(Cipher.ENCRYPT_MODE, key, iv, algorithm)
    return cipher.doFinal(this)
}

/**
 * Aes decrypt byte array
 * @param key the decryption key
 * @param iv the IV (CFB,CBC,CTR need IV)
 * @param algorithm the algorithm parameters
 */
fun ByteArray.aesDecrypt(
    key: ByteArray,
    iv: ByteArray = ByteArray(16),
    algorithm: String = AES_CFB_NOPADDING
): ByteArray {
    val cipher = initCipher(Cipher.DECRYPT_MODE, key, iv, algorithm)
    return cipher.doFinal(this)
}

/**
 * Aes encrypt file
 * @param key the encryption key
 * @param iv the IV (CFB,CBC,CTR need IV)
 * @param destFilePath dest encrypted file
 * @param algorithm the algorithm parameters
 */
fun File.aesEncrypt(
    key: ByteArray,
    iv: ByteArray,
    destFilePath: String,
    algorithm: String = AES_CFB_NOPADDING
): File? {
    return handleFile(Cipher.ENCRYPT_MODE, key, iv, algorithm, path, destFilePath)
}

/**
 * Aes decrypt file
 * @param key the decryption key
 * @param iv the IV (CFB,CBC,CTR need IV)
 * @param destFilePath dest decrypted file
 * @param algorithm the algorithm parameters
 */
fun File.aesDecrypt(
    key: ByteArray,
    iv: ByteArray,
    destFilePath: String,
    algorithm: String = AES_CFB_NOPADDING
): File? {
    return handleFile(Cipher.DECRYPT_MODE, key, iv, algorithm, path, destFilePath)
}

/**
 * Generate aes key byte array , default size is 128
 */
fun initAESKey(size: Int = 128): ByteArray {
    val kg = KeyGenerator.getInstance(KEY_ALGORITHM)
    kg.init(size)
    return kg.generateKey().encoded
}

private fun toKey(key: ByteArray): Key = SecretKeySpec(key, KEY_ALGORITHM)

/**
 * Init Cipher
 * @param mode the operation mode of this cipher
 * @param key the encrypt/decrypt key
 * @param iv the IV
 * @param algorithm the algorithm parameters
 */
fun initCipher(
    mode: Int,
    key: ByteArray,
    iv: ByteArray = ByteArray(16),
    algorithm: String
): Cipher {
    val k = toKey(key)
    val cipher = Cipher.getInstance(algorithm)
    val cipherAlgorithm = algorithm.uppercase(Locale.getDefault())
    if (cipherAlgorithm.contains("CFB") || cipherAlgorithm.contains("CBC")
        || cipherAlgorithm.contains("CTR")
    )
        cipher.init(mode, k, IvParameterSpec(iv))
    else
        cipher.init(mode, k)
    return cipher
}

private fun handleFile(
    mode: Int,
    key: ByteArray,
    iv: ByteArray,
    cipherAlgorithm: String = AES_CFB_NOPADDING,
    sourceFilePath: String,
    destFilePath: String
): File? {
    val sourceFile = File(sourceFilePath)
    val destFile = File(destFilePath)

    if (sourceFile.exists() && sourceFile.isFile) {
        if (!destFile.parentFile!!.exists()) destFile.parentFile!!.mkdirs()
        destFile.createNewFile()

        val inputStream = FileInputStream(sourceFile)
        val outputStream = FileOutputStream(destFile)
        val cipher = initCipher(mode, key, iv, cipherAlgorithm)
        val cin = CipherInputStream(inputStream, cipher)

        val b = ByteArray(1024)
        var read: Int
        do {
            read = cin.read(b)
            if (read > 0)
                outputStream.write(b, 0, read)
        } while (read > 0)

        outputStream.flush()
        cin.close()
        inputStream.close()
        outputStream.close()

        return destFile
    }
    return null
}