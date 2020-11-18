//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "Example"
version = "1.0-SNAPSHOT"

plugins {
//    application
    kotlin("jvm") version "1.4.20-RC"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    google()
}

//java {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//}
//
//tasks.withType<KotlinCompile>().all {
//    kotlinOptions.jvmTarget = "1.8"
//}

//application {
//    mainClassName = "MainKt"
//}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("ch.qos.logback:logback-classic:1.2.3")
    api("io.github.microutils:kotlin-logging-jvm:2.0.3")

    api("com.whirvis:jraknet:2.12.0")

    testApi(group = "junit", name = "junit", version = "4.12")
}
