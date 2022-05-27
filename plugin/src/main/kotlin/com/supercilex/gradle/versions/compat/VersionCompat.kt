package com.supercilex.gradle.versions.compat

object VersionCompat {
    val ANDROID_GRADLE_PLUGIN_VERSION: String by lazy {
        (findClassOrNull("com.android.Version")
                ?: findClassOrNull("com.android.builder.model.Version"))?.let {
            return@lazy it.getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null) as String
        }
        error("Unable to get AGP version.")
    }
}
