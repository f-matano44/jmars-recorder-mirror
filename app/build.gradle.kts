/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects,
 * please refer to https://docs.gradle.org/8.2/userguide/building_java_projects.html
 * in the Gradle documentation.
 */

import java.net.InetAddress
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

val appFullName = "jMARS_Recorder"
val appVersion = "20241122"
val mainClassName = "jp.f_matano44.jmars_recorder.Main"
val license = "GPLv3 (or later)"
val copyright = "Copyright 2023 Fumiyoshi MATANO"

plugins {
    // Apply the application plugin
    // to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven { url = uri("https://www.jitpack.io") }
}

dependencies {
    // for application
    // implementation("groupID:artifactID:version")
    implementation("com.gitlab.f-matano44:jfloatwavio:4.0.0")
    implementation("uk.co.caprica:vlcj:4.7.3")
    // implementation("org.yaml:snakeyaml:2.3")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

application {
    mainClass.set(mainClassName)
}

tasks.jar {
    archiveBaseName.set(appFullName)
    archiveVersion.set(appVersion)
    manifest.attributes["Main-Class"] = mainClassName

    // save build information
    val buildPropertiesFile = "app/src/main/resources/build-info.properties"
    val props = Properties()
    props.setProperty("app.name", appFullName)
    props.setProperty("app.version", appVersion)
    val currentDateTime = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXX")
    val formattedDateTime = formatter.format(currentDateTime)
    props.setProperty("build.date", formattedDateTime)
    val userName = System.getProperty("user.name")
    val hostName = InetAddress.getLocalHost().hostName
    props.setProperty("build.by", userName + "@" + hostName)
    props.setProperty("copyright", copyright)
    props.setProperty("git.head", getGitCommitHash(short = true))
    props.setProperty("license", license)
    File(buildPropertiesFile).outputStream().use {
        props.store(it, null)
    }

    // fat-jar setting
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map{
        if (it.isDirectory) it else zipTree(it)
    })
}

fun getGitCommitHash(short: Boolean = false): String {
    val outputStream = ByteArrayOutputStream()
    val command = if (short)
        listOf("git", "rev-parse", "--short", "HEAD")
    else
        listOf("git", "rev-parse", "HEAD")

    exec {
        commandLine = command
        standardOutput = outputStream
    }

    return outputStream.toString().trim()
}
