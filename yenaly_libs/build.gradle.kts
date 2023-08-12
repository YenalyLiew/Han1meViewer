plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 20

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        dataBinding = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn" + "-Xskip-prerelease-check"
    }
    resourcePrefix = "yenaly_"
    namespace = "com.yenaly.yenaly_libs"
}

dependencies {

    // 全局使用
    api("com.github.cymchad:baserecyclerviewadapterhelper:3.0.7")
    api("com.itxca.spannablex:spannablex:1.0.4")
    api("androidx.recyclerview:recyclerview:1.3.1")
    api("io.github.scwang90:refresh-layout-kernel:2.0.5") // 未添加Header，Footer

    // 模块内使用
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("androidx.startup:startup-runtime:1.1.1")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}