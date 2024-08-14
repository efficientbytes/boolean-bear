plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "app.efficientbytes.booleanbear"
    compileSdk = 34
    ndkPath = "C:/Users/Anubh/AppData/Local/Android/Sdk/ndk/27.0.12077973"
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "app.efficientbytes.booleanbear"
        minSdk = 24
        targetSdk = 34
        versionCode = 21
        versionName = "1.11.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        val debugT10PauseTime = 4L
        val debugT10AdsToShow = 1
        val releaseT10PauseTime = 10L
        val releaseT10AdsToShow = 1


        release {
            manifestPlaceholders["AD_MOB_PUB_ID"] = "ca-app-pub-4895323871451209~2653416909"
            buildConfigField(
                "String",
                "AD_MOB_UNIT_ID",
                "\"ca-app-pub-4895323871451209/7334897949\""
            )

            buildConfigField(
                "long",
                "t10PauseTime",
                "$releaseT10PauseTime"
            )
            buildConfigField(
                "int",
                "t10AdsToShow",
                "$releaseT10AdsToShow"
            )
            ndk.debugSymbolLevel = "FULL"
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            manifestPlaceholders["AD_MOB_PUB_ID"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField(
                "String",
                "AD_MOB_UNIT_ID",
                "\"ca-app-pub-3940256099942544/5224354917\""
            )

            buildConfigField(
                "long",
                "t10PauseTime",
                "$debugT10PauseTime"
            )
            buildConfigField(
                "int",
                "t10AdsToShow",
                "$debugT10AdsToShow"
            )
            isMinifyEnabled = true
            isShrinkResources = true
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
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}
val moshiVersion = "1.15.0"
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    //navigation UI
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    //coil
    implementation("io.coil-kt:coil:2.4.0")
    // Koin
    implementation("io.insert-koin:koin-android:3.6.0-wasm-alpha2")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // moshi
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    //shimmer layout
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    //for splash api
    implementation("androidx.core:core-splashscreen:1.0.1")
    //otp pin view
    implementation("com.github.aabhasr1:OtpView:v1.1.2-ktx")
    //room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //moshi
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //firebase
    //Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    //for server time
    implementation("commons-net:commons-net:3.9.0")
    //rate us play store
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")
    //recycler view (latest for state restoration)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    //media 3 implementation
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.0")
    //gif loader
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
    //okk http
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    //fcm notifications
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    //crashlytics
    implementation("com.google.firebase:firebase-crashlytics:18.6.4")
    //admob
    implementation("com.google.android.gms:play-services-ads:23.1.0")
    //work manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    //country code picker
    implementation("com.hbb20:ccp:2.5.0")
    //play integrity
    implementation("com.google.android.play:integrity:1.3.0")
    //app check play integrity
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-debug")
}