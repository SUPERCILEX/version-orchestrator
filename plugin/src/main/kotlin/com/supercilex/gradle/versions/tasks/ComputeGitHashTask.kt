package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.execWithOutput
import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
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
            private val executor: WorkerExecutor
    ) : WorkAction<Computer.Params> {
        override fun execute() {
            executor.noIsolation().submit(HashComputer::class) {
                rev.set(parameters.revs.file("current-git-rev.txt"))
            }
            executor.noIsolation().submit(StateComputer::class) {
                state.set(parameters.revs.file("cache-invalidation.txt"))
            }
        }

        interface Params : WorkParameters {
            val revs: DirectoryProperty
        }
    }

    abstract class HashComputer @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<HashComputer.Params> {
        override fun execute() {
            val hash = execOps.execWithOutput {
                commandLine("git", "rev-parse", "HEAD")
            }

            parameters.rev.get().asFile.safeCreateNewFile().writeText(hash)
        }

        interface Params : WorkParameters {
            val rev: RegularFileProperty
        }
    }

    abstract class StateComputer @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<StateComputer.Params> {
        override fun execute() {
            val state = execOps.execWithOutput {
                commandLine("git", "describe", "--all", "--dirty")
            }

            parameters.state.get().asFile.safeCreateNewFile().writeText(state)
        }

        interface Params : WorkParameters {
            val state: RegularFileProperty
        }
    }
}
