plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bookexchange"
    compileSdk = 36 // Стандардизирана верзија за компатибилност

    defaultConfig {
        applicationId = "com.example.bookexchange"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Стандардна чиста команда за исклучување оптимизација
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Твоите стандардни Android библиотеки
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)

    // Библиотеки за логирање со Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.material)

    // БИБЛИОТЕКИТЕ ЗА FIREBASE (Чисти и точни)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore) // Еве ја базата, без грешки со две точки!
    implementation("com.google.android.material:material:1.12.0")

    // Тестирање
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}