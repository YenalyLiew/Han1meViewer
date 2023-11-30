import Config.Version.createVersionCode
import Config.Version.createVersionName
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        applicationId = "com.yenaly.han1meviewer"
        minSdk = Config.minSdk
        targetSdk = Config.targetSdk
        versionCode = createVersionCode()
        versionName = versionCode.createVersionName(major = 0, minor = 12, patch = 1)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationVariants.all variant@{
                this@variant.outputs.all output@{
                    val output = this@output as BaseVariantOutputImpl
                    output.outputFileName = "Han1meViewer-v${defaultConfig.versionName}.apk"
                }
            }
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

    // permission

    implementation(Libs.Permission.permissionX)

    // view

    implementation(Libs.Core.RecyclerView.refreshLayoutKernel)
    implementation(Libs.Core.RecyclerView.refreshHeaderMaterial)
    implementation(Libs.Core.RecyclerView.refreshFooterClassics)
    implementation(Libs.Core.RecyclerView.multiType)
    implementation(Libs.Core.RecyclerView.baseRecyclerViewAdapterHelper4)
    implementation(Libs.Core.TextView.expandableTextView)
    implementation(Libs.Spannable.spannableX)
    implementation(Libs.Jetpack.Preference.materialPreference)
    implementation(Libs.Activity.about)
    implementation(Libs.View.stateLayout)

    ksp(Libs.Jetpack.roomCompiler)

    testImplementation(Libs.Test.junit)

    androidTestImplementation(Libs.Test.testJunit)
    androidTestImplementation(Libs.Test.testEspressoCore)
}

configurations.all {
    exclude(group = "androidx.appcompat", module = "appcompat")
}