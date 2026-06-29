plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Auto-generate dev keystore on first build
val devKeystoreFile: File = rootProject.file("keystore/dev.jks")
val devStorePassword = "devpassword"
val devKeyAlias = "devkey"
val devKeyPassword = "devpassword"

val generateDevKeystore by tasks.registering {
    outputs.file(devKeystoreFile)
    onlyIf { !devKeystoreFile.exists() }
    doLast {
        devKeystoreFile.parentFile.mkdirs()
        val keytool = File(System.getProperty("java.home"), "bin/keytool").absolutePath
        val process = ProcessBuilder(
            keytool,
            "-genkeypair",
            "-keystore", devKeystoreFile.absolutePath,
            "-storepass", devStorePassword,
            "-alias", devKeyAlias,
            "-keypass", devKeyPassword,
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-dname", "CN=Dev, OU=Dev, O=Telen, L=Paris, S=IDF, C=FR",
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "keytool failed: $output" }
        logger.lifecycle("Generated dev keystore at ${devKeystoreFile.absolutePath}")
    }
}

android {
    namespace = "com.telen.namebattle"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.telen.namebattle"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("dev") {
            storeFile = devKeystoreFile
            storePassword = devStorePassword
            keyAlias = devKeyAlias
            keyPassword = devKeyPassword
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("dev")
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("dev")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures { compose = true }
}

tasks.named("preBuild") {
    dependsOn(generateDevKeystore)
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.activity)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)

    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
