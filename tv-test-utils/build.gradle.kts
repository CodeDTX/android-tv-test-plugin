plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
}

android {
    namespace  = "com.codedtx.tvtest"
    compileSdk = 34

    defaultConfig { minSdk = 24 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui-test-junit4")
    implementation("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation("androidx.test.ext:junit:1.1.5")
    implementation("junit:junit:4.13.2")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url  = uri("https://maven.pkg.github.com/codedtx/android-tv-testing")
            credentials {
                username = project.findProperty("GPR_USER") as String? ?: System.getenv("GPR_USER")
                password = project.findProperty("GPR_KEY")  as String? ?: System.getenv("GPR_KEY")
            }
        }
    }
    publications {
        create<MavenPublication>("release") {
            groupId    = "com.codedtx"
            artifactId = "tv-test-utils"
            version    = project.findProperty("VERSION_NAME") as String? ?: "1.0.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}
