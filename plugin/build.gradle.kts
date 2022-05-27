plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish")
}

dependencies {
    // Compile only to not force a specific AGP version
    compileOnly("com.android.tools.build:gradle:7.0.0")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<ValidatePlugins>().configureEach {
    enableStricterValidation.set(true)
}

val versionName = providers.fileContents(rootProject.layout.projectDirectory.file("version.txt"))
        .asText.forUseAtConfigurationTime().get().trim()
group = "com.supercilex.gradle"
version = versionName

tasks.withType<PublishToMavenRepository>().configureEach {
    isEnabled = versionName.contains("snapshot", true)
}

gradlePlugin {
    plugins.create("versions") {
        id = "com.supercilex.gradle.versions"
        displayName = "Version Orchestrator"
        description = "Version Orchestrator provides an effortless and performant way to " +
                "automate versioning your Android app."
        implementationClass = "com.supercilex.gradle.versions.VersionOrchestratorPlugin"
    }
}

pluginBundle {
    website = "https://github.com/SUPERCILEX/version-orchestrator"
    vcsUrl = "https://github.com/SUPERCILEX/version-orchestrator"
    tags = listOf("android", "version", "versions", "versioning", "publishing")

    mavenCoordinates {
        groupId = project.group as String
        artifactId = "version-orchestrator"
    }
}

publishing {
    repositories {
        maven {
            name = "Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")

            credentials {
                username = providers.environmentVariable("SONATYPE_NEXUS_USERNAME")
                        .forUseAtConfigurationTime().orNull
                password = providers.environmentVariable("SONATYPE_NEXUS_PASSWORD")
                        .forUseAtConfigurationTime().orNull
            }
        }
    }
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("pluginMaven") {
        artifactId = "version-orchestrator"

        pom {
            name.set("Version Orchestrator")
            description.set(
                    "Version Orchestrator provides an effortless and performant way to automate " +
                            "versioning your Android app.")
            url.set("https://github.com/SUPERCILEX/version-orchestrator")

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
                connection.set("scm:git@github.com:SUPERCILEX/version-orchestrator.git")
                developerConnection.set("scm:git@github.com:SUPERCILEX/version-orchestrator.git")
                url.set("https://github.com/SUPERCILEX/version-orchestrator")
            }
        }
    }
}
