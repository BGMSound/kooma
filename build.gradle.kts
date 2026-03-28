plugins {
    kotlin("jvm") version "2.3.10"
}

repositories {
    mavenCentral()
}

subprojects {
    group = property("project.group").toString()
    version = property("project.version").toString()
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    kotlin {
        jvmToolchain(25)
    }

    tasks.test {
        useJUnitPlatform()
    }
}