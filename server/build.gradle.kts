group = "Example"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.20-RC"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core"))
}
