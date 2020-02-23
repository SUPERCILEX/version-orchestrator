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

internal abstract class RetrieveGitTagListTask : DefaultTask() {
    @get:OutputFile
    abstract val tagListFile: RegularFileProperty

    init {
        // We don't know what's changed on the Git side of things
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun retrieveInfo() {
        val executor = project.serviceOf<WorkerExecutor>()
        executor.noIsolation().submit(Retriever::class) {
            tagList.set(tagListFile)
        }
    }

    abstract class Retriever @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<Retriever.Params> {
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
}
