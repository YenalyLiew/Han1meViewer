@file:Suppress("UnstableApiUsage")

import Config.Version.createVersionCode
import Config.Version.createVersionName
import Config.lastCommitSha
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
}

val isRelease: Boolean
    get() = gradle.startParameter.taskNames.any { it.contains("Release") }

android {
    compileSdk = Config.compileSdk

    val commitSha = if (isRelease) lastCommitSha else "8ea5a9c" // 方便调试

    // 先 Github Secrets 再读取环境变量，若没有则读取本地文件
    val signPwd = System.getenv("HA1_KEYSTORE_PASSWORD") ?: File(
        projectDir, "keystore/ha1_keystore_password.txt"
    ).checkIfExists()?.readText().orEmpty()

    val githubToken = System.getenv("HA1_GITHUB_TOKEN") ?: File(
        projectDir, "ha1_github_token.txt"
    ).checkIfExists()?.readText().orEmpty()

    val signConfig = signingConfigs.create("release") {
        storeFile = File(projectDir, "keystore/Han1meViewerKeystore.jks").checkIfExists()
        storePassword = signPwd
        keyAlias = "yenaly"
        keyPassword = signPwd
        enableV3Signing = true
        enableV4Signing = true
    }

    defaultConfig {
        applicationId = "com.yenaly.han1meviewer"
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        versionCode = if (isRelease) createVersionCode() else 1 // 方便调试
        versionName = versionCode.createVersionName(major = 0, minor = 14, patch = 0)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "COMMIT_SHA", "\"$commitSha\"")
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
        buildConfigField("int", "VERSION_CODE", "$versionCode")
        buildConfigField("String", "HA1_GITHUB_TOKEN", "\"${githubToken}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 被迫 false，混淆会导致无法获取父类泛型
            signingConfig = if (isRelease) signConfig else null
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
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
    namespace = "com.yenaly.han1meviewer"
}

dependencies {

    implementation(project(":yenaly_libs"))

    // android related

    implementation(Libs.Core.coreKtx)
    implementation(Libs.Core.appCompat)
    implementation(Libs.Core.material)
    implementation(Libs.Core.coroutinesAndroid)
    implementation(Libs.Core.fragmentKtx)
    implementation(Libs.Core.constraintLayout)
    implementation(Libs.Core.recyclerView)

    implementation(Libs.Jetpack.lifecycleViewModelKtx)
    implementation(Libs.Jetpack.lifecycleRuntimeKtx)
    implementation(Libs.Jetpack.lifecycleLiveDataKtx)
    implementation(Libs.Jetpack.roomRuntime)
    implementation(Libs.Jetpack.roomKtx)
    implementation(Libs.Jetpack.navigationFragmentKtx)
    implementation(Libs.Jetpack.navigationUiKtx)
    implementation(Libs.Jetpack.preferenceKtx)
    implementation(Libs.Jetpack.workRuntime)
    implementation(Libs.Jetpack.workRuntimeKtx)

    // datetime

    implementation(Libs.Datetime.datetime)

    // parse

    implementation(Libs.Parse.serialization)
    implementation(Libs.Parse.jsoup)

    // network

    implementation(Libs.Network.retrofit)
    implementation(Libs.Network.converterSerialization)

    // pic

    implementation(Libs.Pic.coil)

    // popup

    implementation(Libs.Popup.xPopup)
    implementation(Libs.Popup.xPopupExt)

    // video

    implementation(Libs.Video.jiaoziVideoPlayer)

    // view

    implementation(Libs.Core.RecyclerView.refreshLayoutKernel)
    implementation(Libs.Core.RecyclerView.refreshHeaderMaterial)
    implementation(Libs.Core.RecyclerView.refreshFooterClassics)
    implementation(Libs.Core.RecyclerView.multiType)
    implementation(Libs.Core.RecyclerView.baseRecyclerViewAdapterHelper4)
    implementation(Libs.Core.TextView.expandableTextView)
    implementation(Libs.Spannable.spannableX)
    implementation(Libs.Activity.about)
    implementation(Libs.View.stateLayout)

    ksp(Libs.Jetpack.roomCompiler)

    testImplementation(Libs.Test.junit)

    androidTestImplementation(Libs.Test.testJunit)
    androidTestImplementation(Libs.Test.testEspressoCore)
}

/**
 * This function is used to check if a file exists and is a file.
 */
fun File.checkIfExists(): File? = if (exists() && isFile) this else null