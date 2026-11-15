plugins {
    alias(libs.plugins.android.application)
}

val apiBaseUrl = providers.gradleProperty("apiBaseUrl").getOrElse("http://localhost:5080/")

android {
    namespace = "com.uth.pm2examengrupo5"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.uth.pm2examengrupo5"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.play.services.location)
    implementation(libs.osmdroid)
}
