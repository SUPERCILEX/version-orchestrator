package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.execWithOutput
import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal abstract class RetrieveGitInfoTask : DefaultTask() {
    @get:OutputFile
    abstract val commitCountFile: RegularFileProperty

    @get:OutputFile
    abstract val tagListFile: RegularFileProperty

    @get:OutputFile
    abstract val gitDescribeFile: RegularFileProperty

    init {
        // We don't know what's changed on the Git side of things
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun retrieveInfo() {
        val executor = project.serviceOf<WorkerExecutor>()
        executor.noIsolation().submit(Retriever::class) {
            commitCount.set(commitCountFile)
            tagList.set(tagListFile)
            gitDescribe.set(gitDescribeFile)
        }
    }

    abstract class Retriever @Inject constructor(
            private val executor: WorkerExecutor
    ) : WorkAction<Retriever.Params> {
        override fun execute() {
            executor.noIsolation().submit(CommitCountRetriever::class) {
                commitCount.set(parameters.commitCount)
            }
            executor.noIsolation().submit(TagListRetriever::class) {
                tagList.set(parameters.tagList)
            }
            executor.noIsolation().submit(GitDescribeRetriever::class) {
                gitDescribe.set(parameters.gitDescribe)
            }
        }

        interface Params : WorkParameters {
            val commitCount: RegularFileProperty
            val tagList: RegularFileProperty
            val gitDescribe: RegularFileProperty
        }
    }

    abstract class CommitCountRetriever @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<CommitCountRetriever.Params> {
        override fun execute() {
            val commitCount = execOps.execWithOutput {
                commandLine("git", "rev-list", "--count", "HEAD")
            }

            parameters.commitCount.get().asFile.safeCreateNewFile().writeText(commitCount)
        }

        interface Params : WorkParameters {
            val commitCount: RegularFileProperty
        }
    }

    abstract class TagListRetriever @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<TagListRetriever.Params> {
        override fun execute() {
            val tagList = execOps.execWithOutput {
                commandLine("git", "tag", "--merged", "HEAD")
            }

            parameters.tagList.get().asFile.safeCreateNewFile().writeText(tagList)
        }

        interface Params : WorkParameters {
            val tagList: RegularFileProperty
        }
    }

    abstract class GitDescribeRetriever @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<GitDescribeRetriever.Params> {
        override fun execute() {
            val gitDescribe = execOps.execWithOutput {
                commandLine("git", "describe", "--tags", "--always", "--dirty")
            }

            parameters.gitDescribe.get().asFile.safeCreateNewFile().writeText(gitDescribe)
        }

        interface Params : WorkParameters {
            val gitDescribe: RegularFileProperty
        }
    }
}
