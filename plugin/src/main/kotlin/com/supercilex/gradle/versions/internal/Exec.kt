package com.supercilex.gradle.versions.internal

import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream

internal fun ExecOperations.execWithOutput(block: ExecSpec.() -> Unit): String {
    val output = ByteArrayOutputStream()
    exec {
        block()
        standardOutput = output
    }
    return output.toString().trim()
}
