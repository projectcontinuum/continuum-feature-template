pluginManagement {
    val continuumPlatformVersion: String by settings
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.projectcontinuum.feature") version continuumPlatformVersion
        id("org.projectcontinuum.feature-java") version continuumPlatformVersion
        id("org.projectcontinuum.worker") version continuumPlatformVersion
    }
}

rootProject.name = "continuum-feature-template"

include(":features:continuum-feature-hello")
include(":features:continuum-feature-hello-java")
include(":worker")
