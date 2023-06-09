package com.buildsrc.kts

/**

 *
 * 依赖声明
 *create by zhusw on 5/6/21 15:45
 */
object Dependencies {


    object Foundation {
        const val loading = "com.foundation.widget:loadingview:1.5.0"
    }

    object Kotlin {
        const val version = "1.6.21"

        /**
         * kotlin 语言核心库，像 let这种操作域拓展
         */
        const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.4"

    }

    object Material {
        const val material = "com.google.android.material:material:1.3.0"
    }

    object Coroutines {
        const val coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"

    }

    object Retrofit {
        const val retorifit = "com.squareup.retrofit2:retrofit:2.9.0"//依赖okhttp 3.14.9
        const val converter_gson = "com.squareup.retrofit2:converter-gson:2.9.0"
        const val gson = "com.google.code.gson:gson:2.8.5"
    }

    object UI {
        const val BaseRecyclerViewAdapterHelper =
            "com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4"

    }

    /**
     * ktx 库清单与版本：https://developer.android.google.cn/kotlin/ktx?hl=zh-cn
     */
    object JetPack {
        const val core_ktx = "androidx.core:core-ktx:1.3.2"
        const val fragment_ktx = "androidx.fragment:fragment-ktx:1.3.3"

        /*Lifecycle 拓展协程*/
        const val lifecycle_runtime_ktx = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"

        /*livedata 拓展协程*/
        const val lifecycle_liveData_ktx = "androidx.lifecycle:lifecycle-livedata-ktx:2.3.1"

        //        "androidx.lifecycle:lifecycle-livedata-core:2.3.1"
        /*viewModel 拓展协程*/
        const val lifecycle_viewModel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1"


    }

    object Glide {
        const val glide = "com.github.bumptech.glide:glide:4.10.0"
        const val compiler = "com.github.bumptech.glide:compiler:4.10.0"
    }
}
