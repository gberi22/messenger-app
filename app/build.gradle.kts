plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
}

android {
    namespace = "ge.ngvalia.messengerapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "ge.ngvalia.messengerapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    ksp(libs.ksp)

    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    // For future auth implementation
    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Add this for Firebase coroutines support
    implementation(libs.kotlinx.coroutines.play.services)

    // Image loading
    implementation(libs.glide)

    // Add these missing dependencies for Activity Result API and RecyclerView
    implementation(libs.androidx.activity.ktx.v182)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
}