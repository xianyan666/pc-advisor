plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pchw.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pchw.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
}

// Copy frontend assets before build
tasks.register<Copy>("copyWebAssets") {
    val staticDir = file("${rootProject.projectDir}/../backend/src/main/resources/static")
    val assetDir = file("${projectDir}/src/main/assets/www")

    doFirst {
        if (assetDir.exists()) {
            assetDir.deleteRecursively()
        }
        assetDir.mkdirs()
    }

    from(staticDir) {
        include("**/*")
    }
    into(assetDir)
}

tasks.named("preBuild") {
    dependsOn("copyWebAssets")
}
