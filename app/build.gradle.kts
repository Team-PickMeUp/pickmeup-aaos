import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// 서버 주소: local.properties 에 `pickmeup.api.baseUrl=...` 로 지정 (git 에 올라가지 않음).
// 지정하지 않으면 에뮬레이터에서 PC 의 localhost:8080 (Spring Boot 기본 포트) 으로 연결합니다.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val apiBaseUrl = localProps.getProperty("pickmeup.api.baseUrl") ?: "http://10.0.2.2:8080/"
// 서버가 준비되기 전까지는 가짜 저장소(FakePickupRepository)를 사용합니다.
val useFakeApi = localProps.getProperty("pickmeup.api.useFake")?.toBoolean() ?: true
// Pleos 내비 서비스가 없는 환경(일반 에뮬레이터 등)에서는 `pickmeup.navi.useFake=true` 로 가짜 내비를 사용합니다.
val useFakeNavi = localProps.getProperty("pickmeup.navi.useFake")?.toBoolean() ?: false
// Pleos TextToSpeech: Pleos Playground My Project 의 Client ID/Secret (git 에 올리지 않음).
// 둘 다 있으면 HYBRID 모드(서버 TTS + 실패 시 온디바이스), 없으면 등록이 필요 없는 ON_DEVICE 모드
val ttsClientId = localProps.getProperty("pickmeup.tts.clientId").orEmpty()
val ttsClientSecret = localProps.getProperty("pickmeup.tts.clientSecret").orEmpty()

android {
    namespace = "com.example.pickmeup"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pickmeup"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("boolean", "USE_FAKE_API", "$useFakeApi")
        buildConfigField("boolean", "USE_FAKE_NAVI", "$useFakeNavi")
        buildConfigField("String", "TTS_CLIENT_ID", "\"$ttsClientId\"")
        buildConfigField("String", "TTS_CLIENT_SECRET", "\"$ttsClientSecret\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.pleos.navi.helper)
    implementation(libs.pleos.text.to.speech)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}