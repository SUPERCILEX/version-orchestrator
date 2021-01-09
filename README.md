<h1 align="center">
    Version Orchestrator
</h1>

<p align="center">
    <a href="https://github.com/SUPERCILEX/version-orchestrator/actions">
        <img src="https://github.com/SUPERCILEX/version-orchestrator/workflows/CI/CD/badge.svg" />
    </a>
    <!-- TODO -->
    <a href="https://plugins.gradle.org/plugin/com.supercilex.gradle.versions">
        <img src="https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/supercilex/gradle/versions/com.supercilex.gradle.versions.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugins%20Portal" />
    </a>
</p>

Version Orchestrator provides an effortless and performant way to automate versioning your Android
app.

## Table of contents

1. [How does it work?](#how-does-it-work)
   1. [Version codes](#version-codes)
   1. [Version names](#version-names)
1. [Installation](#installation)
   1. [Snapshot builds](#snapshot-builds)
1. [Configuring Version Orchestrator](#configuring-version-orchestrator)
   1. [Disabling version code configuration](#disabling-version-code-configuration)
   1. [Disabling version name configuration](#disabling-version-name-configuration)
   1. [Enabling debug build configuration](#enabling-debug-build-configuration)
   1. [For existing apps](#for-existing-apps)

## How does it work?

Version Orchestrator looks at your Git history to compute a version code and name for your app.

### Version codes

The version code is a combination of the number of commits in your repository and your tag history,
enabling support for hotfix releases. The math looks a little like this:

```kt
versionCode = existingAppOffset +
        commitCount +
        numberOfNonPatchTags +
        100 * numberOfNonPatchTagsMinusOneIfIsRelease
```

For example, you have 4 commits and tag a `1.0.0` release (`versionCode = 5`). On your
5th commit, the version code will jump to `106`. You continue making commits until you realize a
critical bug needs to be fixed. Branching off the `1.0.0` release, you fix the bug and tag your
`1.0.1` hotfix (`versionCode = 6`). After merging the hotfix and 3 other commits from your new
features back into master, you create a `1.1.0` release (`versionCode = 110`). On your 11th commit,
the version code will jump to `211`. This continues on, allowing you to make 100 patch releases for
each major or minor release.

### Version names

The version name is a combination of the latest tag, commit hash, dirtiness flag, and variant name.
Currently, it is calculated using [`git describe`](https://git-scm.com/docs/git-describe#_examples)
and your `buildType` name plus `productFlavor` name (if present).

## Installation

Apply the plugin to each individual `com.android.application` module where you want to use Version
Orchestrator through the `plugins {}` DSL:

<details open><summary>Kotlin</summary>

```kt
plugins {
    id("com.android.application")
    id("com.supercilex.gradle.versions") version "0.9.0"
}
```

</details>

<details><summary>Groovy</summary>

```groovy
plugins {
    id 'com.android.application'
    id 'com.supercilex.gradle.versions' version '0.9.0'
}
```

</details>

### Snapshot builds

If you're prepared to cut yourself on the bleeding edge of Version Orchestrator development,
snapshot builds are available from
[Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/com/supercilex/gradle/version-orchestrator/):

<details open><summary>Kotlin</summary>

```kt
buildscript {
    repositories {
        // ...
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        // ...
        classpath("com.supercilex.gradle:version-orchestrator:1.0.0-SNAPSHOT")
    }
}
```

</details>

<details><summary>Groovy</summary>

```groovy
buildscript {
    repositories {
        // ...
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }

    dependencies {
        // ...
        classpath 'com.supercilex.gradle:version-orchestrator:1.0.0-SNAPSHOT'
    }
}
```

</details>

## Configuring Version Orchestrator

Version Orchestrator offers several options to fit your use case.

### Disabling version code configuration

To handle version codes yourself, disable version code configuration:

```kt
versionOrchestrator {
    configureVersionCode.set(false)
}
```

### Disabling version name configuration

To handle version names yourself, disable version name configuration:

```kt
versionOrchestrator {
    configureVersionName.set(false)
}
```

### Enabling debug build configuration

To make debug builds as fast as possible, version codes and names are never changed in debug builds
by default. To enable versioning, enable debug build configuration:

```kt
versionOrchestrator {
    configureDebugBuilds.set(true)
}
```

### For existing apps

If your app already has an established version code, you can tell Version Orchestrator about it:

```kt
versionOrchestrator {
    versionCodeOffset.set(123)
}
```
