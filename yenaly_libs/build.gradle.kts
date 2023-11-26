plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs =
            freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn" + "-Xskip-prerelease-check"
    }
    resourcePrefix = "yenaly_"
    namespace = "com.yenaly.yenaly_libs"
}

dependencies {

    implementation(Libs.Core.recyclerView)
    implementation(Libs.Core.coreKtx)
    implementation(Libs.Core.appCompat)
    implementation(Libs.Core.material)
    implementation(Libs.Core.coroutinesAndroid)

    implementation(Libs.Jetpack.lifecycleLiveDataKtx)
    implementation(Libs.Jetpack.lifecycleViewModelKtx)
    implementation(Libs.Jetpack.preferenceKtx)
    implementation(Libs.Jetpack.startupRuntime)
    implementation(Libs.Parse.gson)

    testImplementation(Libs.Test.junit)

    androidTestImplementation(Libs.Test.testJunit)
    androidTestImplementation(Libs.Test.testEspressoCore)
}