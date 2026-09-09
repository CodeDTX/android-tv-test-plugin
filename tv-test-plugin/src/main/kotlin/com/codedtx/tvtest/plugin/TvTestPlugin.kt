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
                    File(workflowDir, "codedtx-ui-tests.yml"),
                    Templates.uiTestsWorkflow(module, primaryFlavor.gradleName, primaryFlavor.appId)
                )
                writeIfAbsent(
                    File(workflowDir, "codedtx-screenshots.yml"),
                    Templates.screenshotsWorkflow(module, primaryFlavor.gradleName, primaryFlavor.appId)
                )

                // 2. Claude Code skill — written once so /setup-tv-testing is available in the consumer project
                val skillContent = TvTestPlugin::class.java.classLoader
                    ?.getResourceAsStream("commands/setup-tv-testing.md")
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                if (skillContent != null) {
                    val claudeCommandsDir = File(rootDir, ".claude/commands").also { it.mkdirs() }
                    writeIfAbsent(File(claudeCommandsDir, "setup-tv-testing.md"), skillContent)
                }

                // 3. androidTest stubs
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
                println("✓  .github/workflows/codedtx-ui-tests.yml")
                println("✓  .github/workflows/codedtx-screenshots.yml")
                println("✓  androidTest/UiTest.kt")
                println("✓  androidTest/ScreenshotTest.kt")
                println("✓  androidTest/test/AppScreen.kt")
                println("✓  androidTest/test/AppTestTags.kt")
                println("✓  androidTest/test/AppTestConsts.kt")
                println("")
                println("✓  .claude/commands/setup-tv-testing.md")
                println("")
                println("Next:")
                println("  Open this project in Claude Code and run /setup-tv-testing")
                println("  It will detect your Activity, fill placeholders, and validate the setup.")
                println("")
            }
        }
    }

    private fun writeIfAbsent(file: File, content: String) {
        if (!file.exists()) file.writeText(content)
        else println("  (skipped — already exists) ${file.name}")
    }
}
