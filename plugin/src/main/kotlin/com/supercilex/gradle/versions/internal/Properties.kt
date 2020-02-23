package com.supercilex.gradle.versions.internal

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal fun <T> Provider<T>.useIf(
        providers: ProviderFactory,
        condition: Provider<Boolean>
): Provider<T?> = condition.flatMap { yes ->
    if (yes) {
        this
    } else {
        providers.provider { null }
    }
}
