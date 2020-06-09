package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.execWithOutput
import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal abstract class RetrieveGitCommitCountTask @Inject constructor(
        private val executor: WorkerExecutor
) : DefaultTask() {
    @get:OutputFile
    abstract val commitCountFile: RegularFileProperty

    init {
        // We don't know what's changed on the Git side of things
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun retrieveInfo() {
        executor.noIsolation().submit(Retriever::class) {
            commitCount.set(commitCountFile)
        }
    }

    abstract class Retriever @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<Retriever.Params> {
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
}
