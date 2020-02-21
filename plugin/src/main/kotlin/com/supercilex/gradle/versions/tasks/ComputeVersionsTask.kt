package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.execWithOutput
import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
internal abstract class ComputeVersionsTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val revDir: DirectoryProperty

    @get:OutputFile
    abstract val versionCodeFile: RegularFileProperty

    @get:OutputFile
    abstract val versionNameFile: RegularFileProperty

    @TaskAction
    fun computeVersions() {
        val executor = project.serviceOf<WorkerExecutor>()
        executor.noIsolation().submit(Computer::class) {
            hash.set(revDir.file("current-git-rev.txt"))
            versionCode.set(versionCodeFile)
            versionName.set(versionNameFile)
        }
    }

    abstract class Computer @Inject constructor(
            private val executor: WorkerExecutor
    ) : WorkAction<Computer.Params> {
        override fun execute() {
            executor.noIsolation().submit(VersionCodeComputer::class) {
                hash.set(parameters.hash)
                versionCode.set(parameters.versionCode)
            }
            executor.noIsolation().submit(VersionNameComputer::class) {
                versionName.set(parameters.versionName)
            }
        }

        interface Params : WorkParameters {
            val hash: RegularFileProperty
            val versionCode: RegularFileProperty
            val versionName: RegularFileProperty
        }
    }

    abstract class VersionCodeComputer @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<VersionCodeComputer.Params> {
        override fun execute() {
            val hash = parameters.hash.get().asFile.readText()
            val versionCode = execOps.execWithOutput {
                commandLine("git", "rev-list", "--count", hash)
            }

            parameters.versionCode.get().asFile.safeCreateNewFile().writeText(versionCode)
        }

        interface Params : WorkParameters {
            val hash: RegularFileProperty
            val versionCode: RegularFileProperty
        }
    }

    abstract class VersionNameComputer @Inject constructor(
            private val execOps: ExecOperations
    ) : WorkAction<VersionNameComputer.Params> {
        override fun execute() {
            val versionName = execOps.execWithOutput {
                commandLine("git", "describe", "--tags", "--always", "--dirty")
            }

            parameters.versionName.get().asFile.safeCreateNewFile().writeText(versionName)
        }

        interface Params : WorkParameters {
            val versionName: RegularFileProperty
        }
    }
}
