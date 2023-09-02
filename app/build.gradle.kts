/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.2/userguide/building_java_projects.html in the Gradle documentation.
 */

val appName = "mRecCorpus2"
val appVersion = "2.0.0"
val mainModuleName = "jp.f_matano44.mreccorpus2"
val mainClassName = "jp.f_matano44.mreccorpus2.MatanosRecorderForCorpus2"

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.beryx.jlink") version "2.26.0"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Use JUnit Jupiter for testing.
    //testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    //testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // for application
    // implementation("groupID:artifactID:version")
    implementation("com.gitlab.f-matano44:jfloatwavio:1.4.0a")
    implementation("uk.co.caprica:vlcj:4.8.2")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainModule.set(mainModuleName)
    mainClass.set(mainClassName)
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set(appName)
    archiveVersion.set(appVersion)
    manifest.attributes["Main-Class"] = mainClassName
}

jlink {
    jpackage {
        // set LICENSE file path
        val osName = System.getProperty("os.name").lowercase()
        val licenseFilePath = if (osName.contains("win")) {
            "..\\LICENSE.txt"
        } else {
            "../LICENSE.txt"
        }
        installerOptions = installerOptions.plus(listOf("--license-file", licenseFilePath))

        // other options
        if (osName.contains("win")) {
            installerOptions = installerOptions.plus(
                listOf("--win-shortcut", "--win-menu")
            )
        } else if (osName.contains("mac")) {
            installerOptions = installerOptions.plus(
                listOf("--mac-package-identifier", "jp.f-matano44.mRecCorpus2")
            )
        } 
    }
    launcher {
        name = appName
        version = appVersion
    }
}
