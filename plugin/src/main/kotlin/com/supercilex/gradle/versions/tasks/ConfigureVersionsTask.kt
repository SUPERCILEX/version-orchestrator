package com.supercilex.gradle.versions.tasks

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.supercilex.gradle.versions.VersionMasterExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getPluginByName
import javax.inject.Inject

internal abstract class ConfigureVersionsTask @Inject constructor(
        @get:Nested val extension: VersionMasterExtension,
        @get:Internal internal val variant: ApplicationVariant
) : DefaultTask() {
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val versionCodeFile: RegularFileProperty

    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val versionNameFile: RegularFileProperty

    @TaskAction
    fun configureVersions() {
        val basePlugin = project.convention.getPluginByName<BasePluginConvention>("base")
        val versionCode by lazy { versionCodeFile.get().asFile.readText().toLong() }
        val versionName by lazy { versionNameFile.get().asFile.readText() }

        variant.outputs.filterIsInstance<ApkVariantOutput>().forEach { output ->
            if (extension.configureVersionCode.get()) {
                output.versionCodeOverride = versionCode.toInt()
            }

            if (extension.configureVersionName.get()) {
                output.versionNameOverride = versionName
                output.outputFileName = "${basePlugin.archivesBaseName}-${variant.applicationId}-" +
                        "${output.baseName}-$versionName.apk"
            }
        }
    }
}
