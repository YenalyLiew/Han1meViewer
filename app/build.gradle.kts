import java.util.Date
import java.text.SimpleDateFormat
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.yenaly.han1meviewer"
        minSdk = 24
        targetSdk = 33
        versionCode = createVersionCode()
        versionName = createVersionName(major = 0, minor = 5, patch = 2)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencies {

    implementation(project(":yenaly_libs"))

    // android related
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.fragment:fragment-ktx:1.5.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
    implementation("androidx.navigation:navigation-fragment:2.5.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.work:work-runtime:2.7.1")
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    // network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // pic
    implementation("io.coil-kt:coil:2.1.0")

    // popup
    implementation("com.github.li-xiaojun:XPopup:2.8.3")
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
    implementation("com.github.mancj:MaterialSearchBar:0.8.5")


    kapt("androidx.room:room-compiler:2.4.3")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
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