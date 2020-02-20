package com.supercilex.gradle.versions

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.supercilex.gradle.versions.internal.VERSION_MASTER_PATH
import com.supercilex.gradle.versions.internal.VersionMasterRootPlugin
import com.supercilex.gradle.versions.tasks.ComputeVersionsTask
import com.supercilex.gradle.versions.tasks.ConfigureVersionsTask
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

        android.applicationVariants.whenObjectAdded {
            val variantName = name.capitalize()

            val configureVersions = project.tasks.register<ConfigureVersionsTask>(
                    "configure${variantName}Versions",
                    extension,
                    this
            )
            configureVersions {
                val versionsTask = project.rootProject.tasks.named(
                        "computeAppVersions", ComputeVersionsTask::class)

                versionCodeFile.set(versionsTask.flatMap { it.versionCodeFile })
                versionNameFile.set(versionsTask.flatMap { it.versionNameFile })
            }
            preBuildProvider {
                dependsOn(configureVersions)
            }
        }
    }
}
