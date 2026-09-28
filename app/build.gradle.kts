import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Compilador do Jetpack Compose (transforma as funções @Composable).
    alias(libs.plugins.kotlin.compose)
    // Serialização: usada pelas rotas de navegação (ver ui/WorkBuddyNavHost.kt).
    alias(libs.plugins.kotlin.serialization)
    // KSP corre o processador do Room, que gera o código SQL a partir das anotações.
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "com.campsc.workbuddy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.campsc.workbuddy"
        // Android 8.0+. Permite usar java.time (datas) sem truques extra.
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

room {
    // O Room guarda aqui um JSON com o esquema de cada versão da base de dados.
    // Faz commit destes ficheiros: são precisos para migrações automáticas.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (UI)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModel + ler Flows de forma segura no Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navegação entre ecrãs
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Room (base de dados SQLite)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Testes unitários (correm no teu PC, sem emulador)
    testImplementation(libs.junit)
}
