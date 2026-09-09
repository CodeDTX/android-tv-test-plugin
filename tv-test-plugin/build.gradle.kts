plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group   = "com.codedtx"
version = findProperty("VERSION_NAME") as String? ?: "1.0.0"

gradlePlugin {
    plugins {
        create("tvTestPlugin") {
            id                  = "com.codedtx.tv-test"
            implementationClass = "com.codedtx.tvtest.plugin.TvTestPlugin"
            displayName         = "Android TV Test Plugin"
            description         = "Scaffolds UI tests and CI workflows for Android TV projects"
        }
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.7.3")
}

// Inject VERSION_NAME into the bundled version.properties so BuildConfig reads the real version.
tasks.named<ProcessResources>("processResources") {
    val pluginVersion = project.findProperty("VERSION_NAME") as String? ?: "1.0.0"
    inputs.property("pluginVersion", pluginVersion)
    filesMatching("version.properties") {
        filter { line -> line.replace("@version@", pluginVersion) }
    }
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
    // Repository only — version is set via project.version above so java-gradle-plugin
    // picks it up for both the JAR publication and the Plugin Marker Artifact.
}
