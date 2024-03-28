plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "app.efficientbytes.booleanbear"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.efficientbytes.booleanbear"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
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
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    //for splash api
    implementation ("androidx.core:core-splashscreen:1.0.1")
    //otp pin view
    implementation ("com.github.aabhasr1:OtpView:v1.1.2-ktx")
    //room
    implementation ("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
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

}