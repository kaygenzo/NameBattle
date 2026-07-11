import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.aboutlibraries)
    id("jacoco")
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

val keystoreProperties = Properties()
keystoreProperties.setProperty("storeFile", "../telen.release.jks")
keystoreProperties.setProperty("storePassword", System.getenv("KEYSTORE_PASSWORD").orEmpty())
keystoreProperties.setProperty("keyAlias", System.getenv("KEY_ALIAS").orEmpty())
keystoreProperties.setProperty("keyPassword", System.getenv("KEY_PASSWORD").orEmpty())

android {
    namespace = "com.telen.namebattle"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.telen.namebattle"
        minSdk = 23
        targetSdk = 37
        versionCode = 1_1_0_2 // major / minor / fixes / iteration
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("dev") {
            storeFile = devKeystoreFile
            storePassword = devStorePassword
            keyAlias = devKeyAlias
            keyPassword = devKeyPassword
        }

        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("dev")
            applicationIdSuffix = ".debug"
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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
    debugImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.aboutlibraries.compose.m3)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}

val coverageExcludes = listOf(
    // DI modules
    "**/di/**",
    // Compose screens, previews, components, theme, navigation
    "**/*Screen*",
    "**/*Previews*",
    "**/components/**",
    "**/theme/**",
    "**/navigation/**",
    // App entry points
    "**/MainActivity*",
    "**/NameBattleApp*",
    "**/*Sheet*",
    "**/*Row*",
    // Room / KSP generated
    "**/*_Impl*",
    "**/*Dao_Impl*",
    "**/BuildConfig*",
    // Android infrastructure — require Android runtime, no business logic
    "**/AppPreferences*",
    "**/NameBattleDatabase*",
    "**/dao/**",
    "**/entity/**",
    // PDF rendering — uses android.graphics APIs, not testable in JVM unit tests
    "**/export/**",
)

tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generates JaCoCo coverage report excluding Compose UI and DI"

    reports {
        xml.required = true
        html.required = true
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/html")
        xml.outputLocation = layout.buildDirectory.file("reports/jacoco/report.xml")
    }

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude(coverageExcludes)
        }
    )
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        }
    )
}
