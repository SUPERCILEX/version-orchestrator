package com.supercilex.gradle.versions

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.supercilex.gradle.versions.internal.VERSION_MASTER_PATH
import com.supercilex.gradle.versions.internal.VersionMasterRootPlugin
import com.supercilex.gradle.versions.tasks.ComputeVersionsTask
import com.supercilex.gradle.versions.tasks.ConfigureVersionsTask
import com.supercilex.gradle.versions.tasks.RetrieveGitInfoTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

@Suppress("unused") // Used by Gradle
internal class VersionMasterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.rootProject.plugins.apply(VersionMasterRootPlugin::class)

        project.extensions.create<VersionMasterExtension>(VERSION_MASTER_PATH).apply {
            configureDebugBuilds.convention(false)
            configureVersionCode.convention(true)
            configureVersionName.convention(true)
            versionCodeOffset.convention(0)
        }

        project.plugins.withType<AppPlugin> {
            applyInternal(project)
        }
        project.afterEvaluate {
            if (project.plugins.findPlugin(AppPlugin::class) == null) {
                throw IllegalStateException(
                        "The Android Gradle Plugin was not applied. Version Master " +
                                "cannot be configured.")
            }
        }
    }

    private fun applyInternal(project: Project) {
        val extension = project.extensions.getByType<VersionMasterExtension>()
        val android = project.the<AppExtension>()

        val computeVersions = project.tasks.register<ComputeVersionsTask>(
                "computeAppVersions",
                extension
        )

        computeVersions {
            val versionsTask = project.rootProject.tasks.named(
                    "retrieveGitVersionInfo", RetrieveGitInfoTask::class)

            commitCountFile.set(versionsTask.flatMap { it.commitCountFile })
            tagListFile.set(versionsTask.flatMap { it.tagListFile })
            gitDescribeFile.set(versionsTask.flatMap { it.gitDescribeFile })

            val dir = project.layout.buildDirectory.dir("version-master")
            versionCodeFile.set(dir.map { it.file("version-code.txt") })
            versionNameFile.set(dir.map { it.file("version-name.txt") })
        }

        android.applicationVariants.whenObjectAdded v@{
            val variantName = name.capitalize()

            if (buildType.isDebuggable && !extension.configureDebugBuilds.get()) {
                return@v
            }

            val configureVersions = project.tasks.register<ConfigureVersionsTask>(
                    "configure${variantName}Versions",
                    extension,
                    this
            )

            configureVersions {
                versionCodeFile.set(computeVersions.flatMap { it.versionCodeFile })
                versionNameFile.set(computeVersions.flatMap { it.versionNameFile })
            }
            preBuildProvider {
                dependsOn(configureVersions)
            }
        }
    }
}
