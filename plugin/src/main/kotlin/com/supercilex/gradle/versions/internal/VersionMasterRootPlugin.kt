package com.supercilex.gradle.versions.internal

import com.supercilex.gradle.versions.tasks.RetrieveGitCommitCountTask
import com.supercilex.gradle.versions.tasks.RetrieveGitDescriptionTask
import com.supercilex.gradle.versions.tasks.RetrieveGitTagListTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

@Suppress("unused") // Used by Gradle
internal class VersionMasterRootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        check(project === project.rootProject)
        validateRuntime()

        val workingDir = project.layout.buildDirectory.dir("version-master/git")
        project.tasks.register<RetrieveGitCommitCountTask>("retrieveGitCommitCount") {
            commitCountFile.set(workingDir.map { it.file("commit-count.txt") })
        }
        project.tasks.register<RetrieveGitTagListTask>("retrieveGitTagList") {
            tagListFile.set(workingDir.map { it.file("tag-list.txt") })
        }
        project.tasks.register<RetrieveGitDescriptionTask>("retrieveGitDescription") {
            gitDescribeFile.set(workingDir.map { it.file("git-description.txt") })
        }
    }
}
