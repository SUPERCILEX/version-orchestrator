import org.gradle.api.artifacts.dsl.RepositoryHandler

fun RepositoryHandler.deps() {
    google().content {
        includeGroup("com.android")
        includeGroupByRegex("com\\.android\\..*")
        includeGroupByRegex("com\\.google\\..*")
        includeGroupByRegex("androidx\\..*")
    }

    jcenter()
}

object Config {
    object Libs {
        object All {
            const val agp = "com.android.tools.build:gradle:4.2.0-alpha01"
        }
    }
}
