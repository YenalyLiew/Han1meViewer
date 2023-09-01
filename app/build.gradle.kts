import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.util.Date
import java.text.SimpleDateFormat
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.yenaly.han1meviewer"
        minSdk = 24
        targetSdk = 33
        versionCode = createVersionCode()
        versionName = createVersionName(major = 0, minor = 10, patch = 0)

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
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    namespace = "com.yenaly.han1meviewer"
}

dependencies {

    implementation(project(":yenaly_libs"))

    // android related
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.work:work-runtime:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // pic
    implementation("io.coil-kt:coil:2.1.0")

    // popup
    implementation("com.github.li-xiaojun:XPopup:2.9.19")
    implementation("com.github.li-xiaojun:XPopupExt:0.0.8")

    // video
    implementation("cn.jzvd:jiaozivideoplayer:7.7.0")

    // permission
    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    // view
    implementation("io.github.scwang90:refresh-header-material:2.0.5")
    implementation("io.github.scwang90:refresh-footer-classics:2.0.5")
    implementation("com.github.MZCretin:ExpandableTextView:v1.6.1-x")
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0")
    implementation("com.drakeet.about:about:2.5.1")
    implementation("com.drakeet.multitype:multitype:4.3.0")

    ksp("androidx.room:room-compiler:2.5.2")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

configurations.all {
    exclude(group = "androidx.appcompat", module = "appcompat")
}

fun createVersionName(major: Int, minor: Int, patch: Int, isPreRelease: Boolean = false): String {
    val version = if (isPreRelease) {
        "${major}.${minor}.${patch}-pre+${android.defaultConfig.versionCode}"
    } else "${major}.${minor}.${patch}+${android.defaultConfig.versionCode}"
    return version.also { println("Version Name: $it") }
}

fun createVersionCode() = SimpleDateFormat("yyMMddHH").let {
    it.timeZone = TimeZone.getTimeZone("UTC")
    it.format(Date()).toInt()
}.also { println("Version Code: $it") }