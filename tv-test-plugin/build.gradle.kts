plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

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
        create<MavenPublication>("pluginMaven") {
            groupId    = "com.codedtx"
            artifactId = "tv-test-plugin"
            version    = project.findProperty("VERSION_NAME") as String? ?: "1.0.0"
        }
    }
}
