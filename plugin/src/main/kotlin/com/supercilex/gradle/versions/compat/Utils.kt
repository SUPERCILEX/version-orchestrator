package com.supercilex.gradle.versions.compat

fun findClassOrNull(clazzName: String): Class<*>? = try {
    Class.forName(clazzName)
} catch (ex: ClassNotFoundException) {
    null
}

@Throws(ClassNotFoundException::class)
fun findClassOrThrow(clazzName: String): Class<*> = Class.forName(clazzName)
