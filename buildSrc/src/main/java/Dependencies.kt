object Libs {

    object Activity {
        const val about = "com.drakeet.about:about:2.5.2"
    }

    object Core {
        const val coreKtx = "androidx.core:core-ktx:1.12.0"
        const val appCompat = "androidx.appcompat:appcompat:1.6.1"
        const val material = "com.google.android.material:material:1.11.0"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:1.6.2"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.3.2"

        object TextView {
            const val expandableTextView = "com.github.MZCretin:ExpandableTextView:v1.6.1-x"
        }

        object RecyclerView {
            const val baseRecyclerViewAdapterHelper3 =
                "io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.14"
            const val baseRecyclerViewAdapterHelper4 =
                "io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.3"
            const val refreshLayoutKernel = "io.github.scwang90:refresh-layout-kernel:2.0.5"
            const val refreshHeaderMaterial = "io.github.scwang90:refresh-header-material:2.0.5"
            const val refreshFooterClassics = "io.github.scwang90:refresh-footer-classics:2.0.5"
            const val multiType = "com.drakeet.multitype:multitype:4.3.0"
        }
    }

    object Jetpack {
        const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
        const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
        const val lifecycleLiveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
        const val roomRuntime = "androidx.room:room-runtime:2.6.1"
        const val roomKtx = "androidx.room:room-ktx:2.6.1"
        const val roomCompiler = "androidx.room:room-compiler:2.6.1" // ksp
        const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:2.7.7"
        const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:2.7.7"
        const val preferenceKtx = "androidx.preference:preference-ktx:1.2.1"
        const val workRuntime = "androidx.work:work-runtime:2.9.0"
        const val workRuntimeKtx = "androidx.work:work-runtime-ktx:2.9.0"
        const val startupRuntime = "androidx.startup:startup-runtime:1.1.1"

        object Preference {
            const val materialPreference = "dev.rikka.rikkax.material:material-preference:2.0.0"
        }
    }

    object View {
        const val stateLayout = "com.github.liangjingkanji:StateLayout:1.4.2"
    }

    object Spannable {
        const val spannableX = "com.itxca.spannablex:spannablex:1.0.4"
    }

    object Parse {
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3"
        const val gson = "com.google.code.gson:gson:2.9.0"
        const val jsoup = "org.jsoup:jsoup:1.17.2"
    }

    object Network {
        const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
        const val converterGson = "com.squareup.retrofit2:converter-gson:2.9.0"
        const val converterSerialization =
            "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0"
        const val okHttp = "com.squareup.okhttp3:okhttp:4.12.0"
        const val dnsOverHttps = "com.squareup.okhttp3:okhttp-dnsoverhttps:4.9.0"
    }

    object Pic {
        const val coil = "io.coil-kt:coil:2.6.0"
        const val glide = "com.github.bumptech.glide:glide:4.16.0"
    }

    object Popup {
        const val xPopup = "com.github.li-xiaojun:XPopup:2.9.19"
        const val xPopupExt = "com.github.li-xiaojun:XPopupExt:1.0.1"
    }

    object Video {
        const val jiaoziVideoPlayer = "cn.jzvd:jiaozivideoplayer:7.7.2.3300"
    }

    object Permission {
        const val permissionX = "com.guolindev.permissionx:permissionx:1.7.1"
    }

    object Test {
        const val junit = "junit:junit:4.13.2" // testImplementation
        const val testJunit = "androidx.test.ext:junit:1.1.5" // androidTestImplementation
        const val testEspressoCore =
            "androidx.test.espresso:espresso-core:3.5.1" // androidTestImplementation
    }
}