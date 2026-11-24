plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "br.com.boituva.conselhotutelar"
    compileSdk = 35

    defaultConfig {
        applicationId = namespace
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

val catalogFile = layout.projectDirectory.file("src/main/assets/canais.json")
val validatorFile = rootProject.layout.projectDirectory.file("scripts/verify_content.py")
val verifyContent by tasks.registering(Exec::class) {
    group = "verification"
    description = "Valida as referências e os destinos do catálogo local."
    inputs.files(catalogFile, validatorFile)
    commandLine(
        providers.environmentVariable("PYTHON").getOrElse("python3"),
        validatorFile.asFile.absolutePath,
        catalogFile.asFile.absolutePath,
    )
}

tasks.named("check") {
    dependsOn(verifyContent)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
