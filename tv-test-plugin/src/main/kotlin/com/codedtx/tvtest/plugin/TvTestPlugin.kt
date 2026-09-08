package com.codedtx.tvtest.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class TvTestPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        // Auto-add tv-test-utils — consumer never writes this manually
        project.dependencies.add(
            "androidTestImplementation",
            "com.codedtx:tv-test-utils:${BuildConfig.LIBRARY_VERSION}"
        )

        project.tasks.register("tvTestSetup") {
            group       = "TV Testing"
            description = "Generates CI workflow YAML and androidTest stub files for this module"

            doLast {
                val android   = project.extensions.getByType(AppExtension::class.java)
                val rootDir   = project.rootProject.rootDir
                val module    = project.name
                val baseAppId = android.defaultConfig.applicationId
                    ?: error("defaultConfig.applicationId must be set")

                data class Flavor(val gradleName: String, val appId: String)

                val flavors = android.productFlavors.map { f ->
                    Flavor(
                        gradleName = f.name.replaceFirstChar { it.uppercase() },
                        appId      = baseAppId + (f.applicationIdSuffix ?: ".${f.name.lowercase()}")
                    )
                }.ifEmpty {
                    listOf(Flavor("Debug", baseAppId))
                }

                val primaryFlavor = flavors.first()
                val packageName   = baseAppId

                // 1. .github/workflows/
                val workflowDir = File(rootDir, ".github/workflows").also { it.mkdirs() }
                writeIfAbsent(
                    File(workflowDir, "ui-tests.yml"),
                    Templates.uiTestsWorkflow(module, primaryFlavor.gradleName, primaryFlavor.appId)
                )
                writeIfAbsent(
                    File(workflowDir, "screenshots.yml"),
                    Templates.screenshotsWorkflow(module, primaryFlavor.gradleName, primaryFlavor.appId)
                )

                // 2. androidTest stubs
                val testRoot = File(
                    project.projectDir,
                    "src/androidTest/java/${packageName.replace('.', '/')}"
                ).also { File(it, "test").mkdirs() }

                writeIfAbsent(File(testRoot, "UiTest.kt"),             Templates.uiTestStub(packageName))
                writeIfAbsent(File(testRoot, "ScreenshotTest.kt"),     Templates.screenshotTestStub(packageName))
                writeIfAbsent(File(testRoot, "test/AppScreen.kt"),     Templates.screenObjectStub(packageName))
                writeIfAbsent(File(testRoot, "test/AppTestTags.kt"),   Templates.testTagsStub(packageName))
                writeIfAbsent(File(testRoot, "test/AppTestConsts.kt"), Templates.testConstantsStub(packageName))

                println("")
                println("✓  .github/workflows/ui-tests.yml")
                println("✓  .github/workflows/screenshots.yml")
                println("✓  androidTest/UiTest.kt")
                println("✓  androidTest/ScreenshotTest.kt")
                println("✓  androidTest/test/AppScreen.kt")
                println("✓  androidTest/test/AppTestTags.kt")
                println("✓  androidTest/test/AppTestConsts.kt")
                println("")
                println("Next:")
                println("  1. Replace TODO_YourActivity in UiTest.kt and ScreenshotTest.kt")
                println("  2. Set LOAD_SIGNAL_TEXT in AppTestConsts.kt")
                println("  3. Add your test tags to AppTestTags.kt")
                println("  4. Write @Test methods in UiTest.kt and ScreenshotTest.kt")
                println("")
            }
        }
    }

    private fun writeIfAbsent(file: File, content: String) {
        if (!file.exists()) file.writeText(content)
        else println("  (skipped — already exists) ${file.name}")
    }
}
