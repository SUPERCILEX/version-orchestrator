package com.supercilex.gradle.versions.tasks

import com.supercilex.gradle.versions.internal.safeCreateNewFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.util.VersionNumber
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
internal abstract class ComputeVersionCodeTask @Inject constructor(
        private val executor: WorkerExecutor
) : DefaultTask() {
    @get:Input
    abstract val versionCodeOffset: Property<Long>

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

    @TaskAction
    fun computeVersions() {
        executor.noIsolation().submit(Computer::class) {
            offset.set(versionCodeOffset)
            commitCount.set(commitCountFile)
            tagList.set(tagListFile)
            gitDescribe.set(gitDescribeFile)

            versionCode.set(versionCodeFile)
        }
    }

    abstract class Computer : WorkAction<Computer.Params> {
        override fun execute() {
            val commitCountFileContents = parameters.commitCount.get().asFile.readText()
            val tagListFileContents = parameters.tagList.get().asFile.readText()
            val gitDescribeFileContents = parameters.gitDescribe.get().asFile.readText()

            val tags = tagListFileContents.split("\n")
            val minorTags = tags.map { VersionNumber.parse(it) }.filter { it.micro == 0 }
            val isCleanlyOnTag = gitDescribeFileContents
                    .removePrefix(tags.lastOrNull().orEmpty()).take(1).length xor 1

            val offset = parameters.offset.get()
            val commitCount = commitCountFileContents.toLong()
            val minorTagsCount = minorTags.size
            val hotfixSpacing = (minorTagsCount - isCleanlyOnTag).coerceAtLeast(0) * 100

            val versionCode = (offset + commitCount + minorTagsCount + hotfixSpacing).toString()
            parameters.versionCode.get().asFile.safeCreateNewFile().writeText(versionCode)
        }

        interface Params : WorkParameters {
            val offset: Property<Long>
            val commitCount: RegularFileProperty
            val tagList: RegularFileProperty
            val gitDescribe: RegularFileProperty

            val versionCode: RegularFileProperty
        }
    }
}
