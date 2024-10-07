@file:Suppress("UnstableApiUsage")

import Config.Version.createVersionCode
import Config.Version.createVersionName
import Config.lastCommitSha
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.google.devtools.ksp)
}

val isRelease: Boolean
    get() = gradle.startParameter.taskNames.any { it.contains("Release") }

android {
    compileSdk = property("compile.sdk")?.toString()?.toIntOrNull()

    val commitSha = if (isRelease) lastCommitSha else "8ea5a9c" // 方便调试

    // 先 Github Secrets 再读取环境变量，若没有则读取本地文件
    val signPwd = System.getenv("HA1_KEYSTORE_PASSWORD") ?: File(
        projectDir, "keystore/ha1_keystore_password.txt"
    ).checkIfExists()?.readText().orEmpty()

    val githubToken = System.getenv("HA1_GITHUB_TOKEN") ?: File(
        projectDir, "ha1_github_token.txt"
    ).checkIfExists()?.readText().orEmpty()

    val signConfig = if (isRelease) signingConfigs.create("release") {
        storeFile = File(projectDir, "keystore/Han1meViewerKeystore.jks").checkIfExists()
        storePassword = signPwd
        keyAlias = "yenaly"
        keyPassword = signPwd
        enableV3Signing = true
        enableV4Signing = true
    } else null

    defaultConfig {
        applicationId = "com.yenaly.han1meviewer"
        minSdk = property("min.sdk")?.toString()?.toIntOrNull()
        targetSdk = property("target.sdk")?.toString()?.toIntOrNull()
        versionCode = if (isRelease) createVersionCode() else 1 // 方便调试
        versionName = versionCode.createVersionName(major = 0, minor = 15, patch = 2)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "COMMIT_SHA", "\"$commitSha\"")
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
        buildConfigField("int", "VERSION_CODE", "$versionCode")
        buildConfigField("String", "HA1_GITHUB_TOKEN", "\"${githubToken}\"")

        buildConfigField("int", "SEARCH_YEAR_RANGE_END", "${Config.thisYear}")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signConfig
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            applicationVariants.all variant@{
                this@variant.outputs.all output@{
                    val output = this@output as BaseVariantOutputImpl
                    output.outputFileName = "Han1meViewer-v${defaultConfig.versionName}.apk"
                }
            }
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            applicationIdSuffix = ".debug"
        }
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        buildConfig = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xjvm-default=all-compatibility")
    }
    namespace = "com.yenaly.han1meviewer"
}

dependencies {

    implementation(project(":yenaly_libs"))

    // android related

    implementation(libs.bundles.android.base)
    implementation(libs.bundles.android.jetpack)
    implementation(libs.palette)

    // datetime

    implementation(libs.datetime)

    // parse

    implementation(libs.serialization.json)
    implementation(libs.jsoup)

    // network

    implementation(libs.retrofit)
    implementation(libs.converter.serialization)

    // pic

    implementation(libs.coil)

    // popup

    implementation(libs.xpopup)
    implementation(libs.xpopup.ext)

    // video

    implementation(libs.jiaozi.video.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)

    // view

    implementation(libs.refresh.layout.kernel)
    implementation(libs.refresh.header.material)
    implementation(libs.refresh.footer.classics)
    implementation(libs.multitype)
    implementation(libs.base.recyclerview.adapter.helper4)
    implementation(libs.expandable.textview)
    implementation(libs.spannable.x)
    implementation(libs.about)
    implementation(libs.statelayout)
    implementation(libs.circular.reveal.switch)

    ksp(libs.room.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)

    androidTestImplementation(libs.test.junit)
    androidTestImplementation(libs.test.espresso.core)

    // debugImplementation(libs.leak.canary)
}

/**
 * This function is used to check if a file exists and is a file.
 */
fun File.checkIfExists(): File? = if (exists() && isFile) this else null