package com.supercilex.gradle.versions.compat

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Compat version of [com.android.build.api.variant.AndroidComponentsExtension]
 * - AGP 4.2 package is 'com.android.build.api.extension'
 * - AGP 7.0 package is 'com.android.build.api.variant'
 */
sealed class ApplicationAndroidComponentsExtensionCompat {

    /**
     * A compatibility function of
     * [com.android.build.api.variant.AndroidComponentsExtension.onVariants].
     */
    abstract fun onVariants(block: (ApplicationVariant) -> Unit)

    class Api70Impl(
            private val actual: ApplicationAndroidComponentsExtension
    ) : ApplicationAndroidComponentsExtensionCompat() {

        override fun onVariants(block: (ApplicationVariant) -> Unit) {
            actual.onVariants { variant -> block.invoke(variant) }
        }
    }

    class Api42Impl(
            private val actual: Any
    ) : ApplicationAndroidComponentsExtensionCompat() {

        private val extensionClazz =
                findClassOrThrow("com.android.build.api.extension.AndroidComponentsExtension")

        private val variantSelectorClazz = findClassOrThrow("com.android.build.api.extension.VariantSelector")

        override fun onVariants(block: (ApplicationVariant) -> Unit) {
            val selector = extensionClazz.getDeclaredMethod("selector").invoke(actual)
            val allSelector = variantSelectorClazz.getDeclaredMethod("all").invoke(selector)
            val wrapFunction: (ApplicationVariant) -> Unit = { block.invoke(it) }
            extensionClazz.getDeclaredMethod(
                    "onVariants", variantSelectorClazz, Function1::class.java
            ).invoke(actual, allSelector, wrapFunction)
        }
    }

    companion object {
        fun get(project: Project): ApplicationAndroidComponentsExtensionCompat {
            return if (isAtLeastApi70()) {
                val actualExtension = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
                Api70Impl(actualExtension)
            } else {
                val actualExtension = project.extensions.getByType(
                        findClassOrThrow("com.android.build.api.extension.AndroidComponentsExtension")
                )
                Api42Impl(actualExtension)
            }
        }

        private fun isAtLeastApi70(): Boolean {
            return findClassOrNull("com.android.build.api.variant.AndroidComponentsExtension") != null
        }
    }
}
