plugins {
    id("org.projectcontinuum.worker")
}

group = "org.projectcontinuum.app.worker.example"
description = "Continuum Feature Example Worker — Spring Boot worker application for example feature nodes"
version = "0.0.1"

continuum {
    continuumVersion.set("0.0.6-SNAPSHOT")
}

dependencies {
    implementation(project(":features:continuum-feature-hello"))
    implementation(project(":features:continuum-feature-hello-java"))
}
