package com.supercilex.gradle.versions.internal

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.gradle.util.GradleVersion

private val MIN_GRADLE_VERSION = GradleVersion.version("7.0")
private val MIN_AGP_VERSION = AndroidPluginVersion(7, 0)

internal fun validateRuntime(project: Project) {
    val gradleVersion = GradleVersion.current()

    check(gradleVersion >= MIN_GRADLE_VERSION) {
        """
        |Version Orchestrator's minimum Gradle version is at least $MIN_GRADLE_VERSION and yours
        |is $gradleVersion. Find the latest version at
        |https://github.com/gradle/gradle/releases/latest, then run
        |$ ./gradlew wrapper --gradle-version=${"$"}LATEST --distribution-type=ALL
        """.trimMargin()
    }

    project.plugins.withType<AppPlugin> {
        val agpVersion = project.extensions.findByType<ApplicationAndroidComponentsExtension>()?.pluginVersion

        check(null != agpVersion && agpVersion >= MIN_AGP_VERSION) {
            """
            |Version Orchestrator's minimum Android Gradle Plugin version is at least
            |$MIN_AGP_VERSION and yours is $agpVersion. Find the latest version and upgrade
            |instructions at https://developer.android.com/studio/releases/gradle-plugin.
            """.trimMargin()
        }
    }
}
