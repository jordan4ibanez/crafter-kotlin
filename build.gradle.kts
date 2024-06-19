import org.gradle.internal.os.OperatingSystem

repositories {
  mavenCentral()
  gradlePluginPortal()
}

val lwjglVersion = "3.3.3"
val jomlVersion = "1.10.5"

val lwjglNatives = when (OperatingSystem.current()) {
  OperatingSystem.LINUX -> "natives-linux"
  OperatingSystem.MAC_OS -> "natives-macos"
  OperatingSystem.WINDOWS -> "natives-windows"
  else -> throw Error("This operating system is not supported. Maybe you can help with that?")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")

  runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.0.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

  // groovy
  implementation("org.codehaus.groovy:groovy-all:3.0.21")

  // Apache Ignite database
  implementation("org.apache.ignite:ignite-core:2.15.0")

  // JSON helper
  implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
  implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")

  // LWJGL
  implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

  implementation("org.lwjgl:lwjgl")
  implementation("org.lwjgl:lwjgl-assimp")
  implementation("org.lwjgl:lwjgl-glfw")
  implementation("org.lwjgl:lwjgl-openal")
  implementation("org.lwjgl:lwjgl-opengl")
  implementation("org.lwjgl:lwjgl-stb")

  runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
  runtimeOnly("org.lwjgl:lwjgl-assimp::$lwjglNatives")
  runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
  runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
  runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
  runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
  implementation("org.joml:joml:$jomlVersion")

  runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.0.0")

  testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")
  testImplementation(kotlin("test"))
}

plugins {
  id("java-library")
  kotlin("jvm") version "2.0.0"
  id("application")
}

val kotlinVersion = "2.0.0"

//final group = "org.example"
//final version = "1.0-SNAPSHOT"

tasks.test {
  useJUnitPlatform()
}

// Thanks, chaottic!
sourceSets {
  main {
    java {
//      srcDir("mods")
      exclude("*")
    }
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(22)
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
  }
}

kotlin {
  jvmToolchain(22)
}

application {
  mainClass.set("MainKt")
}

tasks.wrapper {
  gradleVersion = "8.8"
  distributionType = Wrapper.DistributionType.BIN
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
  jvmTargetValidationMode.set(org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.WARNING)
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22)
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}