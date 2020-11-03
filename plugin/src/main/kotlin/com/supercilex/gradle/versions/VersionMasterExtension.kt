package com.supercilex.gradle.versions

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/** The entry point for all Version Orchestrator related configuration. */
abstract class VersionMasterExtension @JvmOverloads constructor(
        @Suppress("unused")
        @get:Internal
        internal val name: String = "default" // Needed for Gradle
) {
    /**
     * Enables or disables version configuration for debug builds.
     *
     * Defaults to `false`.
     */
    @get:Input
    abstract val configureDebugBuilds: Property<Boolean>

    /**
     * Enables or disables version code configuration.
     *
     * Defaults to `true`.
     */
    @get:Input
    abstract val configureVersionCode: Property<Boolean>

    /**
     * Enables or disables version name configuration.
     *
     * Defaults to `true`.
     */
    @get:Input
    abstract val configureVersionName: Property<Boolean>

    /**
     * For existing apps: if you already have an established version code, put it here to tell VM to
     * increase its version code calculation by this amount.
     *
     * Defaults to `0`.
     */
    @get:Input
    abstract val versionCodeOffset: Property<Long>
}
