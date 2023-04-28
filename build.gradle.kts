import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
    }
}

plugins {
    kotlin("jvm") version "1.4.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "11"
            targetCompatibility = "11"
        }

        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
}

val kotlinVersion = "1.4.0"
val coroutinesVersion = "1.3.9"

subprojects {
    apply(plugin = "com.android.application")
    apply(plugin = "kotlin-android")

    dependencies {
        implementation(kotlin("stdlib-jdk7"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    }
}

tasks {
    clean {
        delete(rootProject.buildDir)
    }
}
