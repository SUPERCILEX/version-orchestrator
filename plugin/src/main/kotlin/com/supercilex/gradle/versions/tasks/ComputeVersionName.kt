package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
internal abstract class ComputeVersionName @Inject constructor(
        private val executor: WorkerExecutor
) : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val gitDescribeFile: RegularFileProperty

    @get:OutputFile
    abstract val versionNameFile: RegularFileProperty

    @TaskAction
    fun computeVersions() {
        executor.noIsolation().submit(Computer::class) {
            gitDescribe.set(gitDescribeFile)

            versionName.set(versionNameFile)
        }
    }

    abstract class Computer : WorkAction<Computer.Params> {
        override fun execute() {
            val versionName = parameters.gitDescribe.get().asFile.readText()

            parameters.versionName.get().asFile.safeCreateNewFile().writeText(versionName)
        }

        interface Params : WorkParameters {
            val gitDescribe: RegularFileProperty

            val versionName: RegularFileProperty
        }
    }
}
