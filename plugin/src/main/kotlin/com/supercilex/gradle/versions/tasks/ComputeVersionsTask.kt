package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.VersionMasterExtension
import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.util.VersionNumber
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
internal abstract class ComputeVersionsTask @Inject constructor(
        @get:Nested val extension: VersionMasterExtension
) : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val commitCountFile: RegularFileProperty

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val tagListFile: RegularFileProperty

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val gitDescribeFile: RegularFileProperty

    @get:OutputFile
    abstract val versionCodeFile: RegularFileProperty

    @get:OutputFile
    abstract val versionNameFile: RegularFileProperty

    @TaskAction
    fun computeVersions() {
        val executor = project.serviceOf<WorkerExecutor>()
        executor.noIsolation().submit(Computer::class) {
            versionCodeOffset.set(extension.versionCodeOffset)
            commitCount.set(commitCountFile)
            tagList.set(tagListFile)
            gitDescribe.set(gitDescribeFile)

            versionCode.set(versionCodeFile)
            versionName.set(versionNameFile)
        }
    }

    abstract class Computer @Inject constructor(
            private val executor: WorkerExecutor
    ) : WorkAction<Computer.Params> {
        override fun execute() {
            executor.noIsolation().submit(VersionCodeComputer::class) {
                versionCodeOffset.set(parameters.versionCodeOffset)
                commitCount.set(parameters.commitCount)
                tagList.set(parameters.tagList)
                gitDescribe.set(parameters.gitDescribe)

                versionCode.set(parameters.versionCode)
            }
            executor.noIsolation().submit(VersionNameComputer::class) {
                gitDescribe.set(parameters.gitDescribe)

                versionName.set(parameters.versionName)
            }
        }

        interface Params : WorkParameters {
            val versionCodeOffset: Property<Long>
            val commitCount: RegularFileProperty
            val tagList: RegularFileProperty
            val gitDescribe: RegularFileProperty

            val versionCode: RegularFileProperty
            val versionName: RegularFileProperty
        }
    }

    abstract class VersionCodeComputer : WorkAction<VersionCodeComputer.Params> {
        override fun execute() {
            val commitCountFileContents = parameters.commitCount.get().asFile.readText()
            val tagListFileContents = parameters.tagList.get().asFile.readText()
            val gitDescribeFileContents = parameters.gitDescribe.get().asFile.readText()

            val tags = tagListFileContents.split("\n")
            val minorTags = tags.map { VersionNumber.parse(it) }.filter { it.micro == 0 }
            val isCleanlyOnTag = gitDescribeFileContents
                    .removePrefix(tags.lastOrNull().orEmpty()).take(1).length xor 1

            val offset = parameters.versionCodeOffset.get()
            val commitCount = commitCountFileContents.toLong()
            val hotfixSpacing = (minorTags.size - isCleanlyOnTag).coerceAtLeast(0) * 100

            val versionCode = (offset + commitCount + hotfixSpacing).toString()
            parameters.versionCode.get().asFile.safeCreateNewFile().writeText(versionCode)
        }

        interface Params : WorkParameters {
            val versionCodeOffset: Property<Long>
            val commitCount: RegularFileProperty
            val tagList: RegularFileProperty
            val gitDescribe: RegularFileProperty

            val versionCode: RegularFileProperty
        }
    }

    abstract class VersionNameComputer : WorkAction<VersionNameComputer.Params> {
        override fun execute() {
            val gitDescribeFileContents = parameters.gitDescribe.get().asFile.readText()

            val versionName = gitDescribeFileContents
            parameters.versionName.get().asFile.safeCreateNewFile().writeText(versionName)
        }

        interface Params : WorkParameters {
            val gitDescribe: RegularFileProperty

            val versionName: RegularFileProperty
        }
    }
}
