import java.util.Properties
import org.gradle.api.Project

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

fun localProperty(project: Project, key: String): String? {
    val f = project.rootProject.file("local.properties")
    if (!f.exists()) return null
    return f.inputStream().use { stream ->
        Properties().apply { load(stream) }.getProperty(key)
    }
}

android {
    namespace = "com.latertogether.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.latertogether.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        val supabaseUrl = listOf(
            localProperty(project, "SUPABASE_URL"),
            project.findProperty("SUPABASE_URL") as String?,
            project.providers.gradleProperty("SUPABASE_URL").orNull,
        ).firstOrNull { !it.isNullOrBlank() } ?: ""
        val supabaseKey = listOf(
            localProperty(project, "SUPABASE_ANON_KEY"),
            project.findProperty("SUPABASE_ANON_KEY") as String?,
            project.providers.gradleProperty("SUPABASE_ANON_KEY").orNull,
        ).firstOrNull { !it.isNullOrBlank() } ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
