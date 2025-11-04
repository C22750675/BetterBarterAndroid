plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.hugogarry.betterbarter"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
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
        // This is the configuration for your development builds
        getByName("debug") {
            // Generates: public static final String BASE_URL = "http://10.0.2.2:3000/api/";
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")
        }

        // This is the configuration for your production/release builds
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Generates: public static final String BASE_URL = "http://10.0.2.2:3000/api/";
            // TODO: REPLACE with actual production URL
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
}