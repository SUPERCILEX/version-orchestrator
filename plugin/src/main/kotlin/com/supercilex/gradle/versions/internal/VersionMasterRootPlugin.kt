package com.supercilex.gradle.versions.internal

import com.supercilex.gradle.versions.tasks.ComputeVersionsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

@Suppress("unused") // Used by Gradle
internal class VersionMasterRootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project === project.rootProject)
        validateRuntime()

        project.tasks.register<ComputeVersionsTask>("computeAppVersions") {
            versionCodeFile.set(project.layout.buildDirectory.file(
                    "version-master/version-code.txt"))
            versionNameFile.set(project.layout.buildDirectory.file(
                    "version-master/version-name.txt"))
        }
    }
}
