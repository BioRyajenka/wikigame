group = "Example"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.20-RC"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    google()
}

dependencies {
    api(project(":core"))
    compile("com.gitlab.mvysny.konsume-xml:konsume-xml:0.12")
}
