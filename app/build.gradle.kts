plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs.kotlin)
    id("kotlin-parcelize")
}

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.hugogarry.betterbarter"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }
    flavorDimensions += "targetDevice"

    productFlavors {
        create("emulator") {
            dimension = "targetDevice"
            buildConfigField("String", "BASE_URL", "\"http://10.156.13.143:3000/api/\"")
        }
        create("physical") {
            dimension = "targetDevice"
            buildConfigField("String", "BASE_URL", "\"http://10.156.13.143:3000/api/\"")
        }
    }

    defaultConfig {
        applicationId = "com.hugogarry.betterbarter"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // This is a global setting for all 'release' builds.
            buildConfigField("String", "BASE_URL", "\"https://api.yourproduction.com/api/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.location)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.logging.interceptor.v4120)
    implementation(libs.coil)
}