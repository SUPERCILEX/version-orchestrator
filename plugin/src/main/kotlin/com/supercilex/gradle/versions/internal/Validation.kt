package com.supercilex.gradle.versions.internal

import com.supercilex.gradle.versions.compat.VersionCompat
import org.gradle.util.GradleVersion
import org.gradle.util.VersionNumber

private val MIN_GRADLE_VERSION = GradleVersion.version("6.8")
private val MIN_AGP_VERSION = VersionNumber.parse("4.2.0-beta03")

internal fun validateRuntime() {
    val gradleVersion = GradleVersion.current()
    val agpVersion = VersionNumber.parse(VersionCompat.ANDROID_GRADLE_PLUGIN_VERSION)

    check(gradleVersion >= MIN_GRADLE_VERSION) {
        """
        |Version Orchestrator's minimum Gradle version is at least $MIN_GRADLE_VERSION and yours
        |is $gradleVersion. Find the latest version at
        |https://github.com/gradle/gradle/releases/latest, then run
        |$ ./gradlew wrapper --gradle-version=${"$"}LATEST --distribution-type=ALL
        """.trimMargin()
    }

    check(agpVersion >= MIN_AGP_VERSION) {
        """
        |Version Orchestrator's minimum Android Gradle Plugin version is at least
        |$MIN_AGP_VERSION and yours is $agpVersion. Find the latest version and upgrade
        |instructions at https://developer.android.com/studio/releases/gradle-plugin.
        """.trimMargin()
    }
}
