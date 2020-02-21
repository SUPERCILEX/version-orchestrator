plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

dependencies {
    compileOnly(Config.Libs.All.agp) // Compile only to not force a specific AGP version
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<ValidatePlugins>().configureEach {
    enableStricterValidation.set(true)
}

val versionName = rootProject.file("version.txt").readText().trim()
group = "com.supercilex.gradle"
version = versionName

tasks.withType<PublishToMavenRepository>().configureEach {
    isEnabled = versionName.contains("snapshot", true)
}

gradlePlugin {
    plugins.create("versions") {
        id = "com.supercilex.gradle.versions"
        displayName = "Version Master"
        description = "Version Master provides an effortless and performant way to automate " +
                "versioning your Android app."
        implementationClass = "com.supercilex.gradle.versions.VersionMasterPlugin"
    }
}

pluginBundle {
    website = "https://github.com/SUPERCILEX/version-master"
    vcsUrl = "https://github.com/SUPERCILEX/version-master"
    tags = listOf("android", "version", "versions", "versioning", "publishing")

    mavenCoordinates {
        groupId = project.group as String
        artifactId = "version-master"
    }
}

publishing {
    repositories {
        maven {
            name = "Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")

            credentials {
                username = System.getenv("SONATYPE_NEXUS_USERNAME")
                password = System.getenv("SONATYPE_NEXUS_PASSWORD")
            }
        }
    }
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("pluginMaven") {
        artifactId = "version-master"

        pom {
            name.set("Version Master")
            description.set(
                    "Version Master provides an effortless and performant way to automate " +
                            "versioning your Android app.")
            url.set("https://github.com/SUPERCILEX/version-master")

            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://opensource.org/licenses/Apache-2.0")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("SUPERCILEX")
                    name.set("Alex Saveau")
                    email.set("saveau.alexandre@gmail.com")
                    roles.set(listOf("Owner"))
                    timezone.set("-8")
                }
            }

            scm {
                connection.set("scm:git@github.com:SUPERCILEX/version-master.git")
                developerConnection.set("scm:git@github.com:SUPERCILEX/version-master.git")
                url.set("https://github.com/SUPERCILEX/version-master")
            }
        }
    }
}
