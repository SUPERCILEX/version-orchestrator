package com.supercilex.gradle.versions.internal

import com.supercilex.gradle.versions.tasks.RetrieveGitInfoTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

@Suppress("unused") // Used by Gradle
internal class VersionMasterRootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project === project.rootProject)
        validateRuntime()

        project.tasks.register<RetrieveGitInfoTask>("retrieveGitVersionInfo") {
            val dir = project.layout.buildDirectory.dir("version-master/git")
            commitCountFile.set(dir.map { it.file("commit-count.txt") })
            tagListFile.set(dir.map { it.file("tag-list.txt") })
            gitDescribeFile.set(dir.map { it.file("version-name.txt") })
        }
    }
}
