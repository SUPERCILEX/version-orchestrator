package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.execWithOutput
import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal abstract class ComputeGitHashTask : DefaultTask() {
    @get:OutputDirectory
    abstract val revDir: DirectoryProperty

    init {
        // We don't know what's changed on the Git side of things
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun computeGitHash() {
        val executor = project.serviceOf<WorkerExecutor>()
        executor.noIsolation().submit(Computer::class) {
            revs.set(revDir)
        }
    }

    abstract class Computer @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<Computer.Params> {
        override fun execute() {
            val dirty = execOps.execWithOutput {
                commandLine("git", "status", "--porcelain")
            }.isNotEmpty()
            val tags = execOps.execWithOutput {
                commandLine("git", "tag", "--points-at", "HEAD")
            }
            val hash = execOps.execWithOutput {
                commandLine("git", "rev-parse", "HEAD")
            }

            val currentRev = parameters.revs.file("current-git-rev.txt")
                    .get().asFile.safeCreateNewFile()
            val cacheInvalidation = parameters.revs.file("cache-invalidation.txt")
                    .get().asFile.safeCreateNewFile()

            currentRev.writeText(hash)
            cacheInvalidation.writeText(listOf(tags, dirty).joinToString("\n"))
        }

        interface Params : WorkParameters {
            val revs: DirectoryProperty
        }
    }
}

