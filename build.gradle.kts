import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    `lifecycle-base`
    id("com.github.ben-manes.versions") version "0.36.0"

    kotlin("jvm") version "1.4.21" apply false
    id("com.gradle.plugin-publish") version "0.12.0" apply false
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

allprojects {
    repositories {
        google().content {
            includeGroup("com.android")
            includeGroupByRegex("com\\.android\\..*")
            includeGroupByRegex("com\\.google\\..*")
            includeGroupByRegex("androidx\\..*")
        }

        jcenter()
    }

    afterEvaluate {
        convention.findByType<JavaPluginExtension>()?.apply {
            sourceCompatibility = JavaVersion.VERSION_1_8
        }

        convention.findByType<KotlinProjectExtension>()?.apply {
            sourceSets.configureEach {
                languageSettings.progressiveMode = true
                languageSettings.enableLanguageFeature("NewInference")
            }
        }
    }

    tasks.withType<Test> {
        maxParallelForks = Runtime.getRuntime().availableProcessors()

        testLogging {
            events("passed", "failed", "skipped")
            showStandardStreams = true
            setExceptionFormat("full")
        }
    }
}
