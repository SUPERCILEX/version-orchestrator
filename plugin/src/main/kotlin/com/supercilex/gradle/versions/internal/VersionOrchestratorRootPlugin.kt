package com.supercilex.gradle.versions.internal

import com.supercilex.gradle.versions.tasks.RetrieveGitCommitCount
import com.supercilex.gradle.versions.tasks.RetrieveGitDescription
import com.supercilex.gradle.versions.tasks.RetrieveGitTagList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

@Suppress("unused") // Used by Gradle
internal class VersionOrchestratorRootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project === project.rootProject)

        validateRuntime(project)

        val workingDir = project.layout.buildDirectory.dir("version-orchestrator/git")
        project.tasks.register<RetrieveGitCommitCount>("retrieveGitCommitCount") {
            commitCountFile.set(workingDir.map { it.file("commit-count.txt") })
        }
        project.tasks.register<RetrieveGitTagList>("retrieveGitTagList") {
            tagListFile.set(workingDir.map { it.file("tag-list.txt") })
        }
        project.tasks.register<RetrieveGitDescription>("retrieveGitDescription") {
            gitDescribeFile.set(workingDir.map { it.file("git-description.txt") })
        }
    }
}
