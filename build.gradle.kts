import org.gradle.internal.os.OperatingSystem

repositories {
  mavenCentral()
  gradlePluginPortal()
}

plugins {
  id("application")
  kotlin("jvm") version "2.0.0"
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

  implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

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

  // Junit5 testing wasteland
  //! This doesn't work
//  testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")
//  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.0")

  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val kotlinVersion = "2.0.0"

//final group = "org.example"
//final version = "1.0-SNAPSHOT"

//tasks.named<Test>("test") {
//  useJUnitPlatform()
//  testLogging {
//    events("passed")
//  }
//}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.test {
  useJUnitPlatform()
//  testLogging {
//    events("passed", "skipped", "failed")
//  }
  testLogging {
    showStandardStreams = true
  }
}

// Thanks, chaottic!
sourceSets {
  main {
    kotlin {
//      srcDir("mods")
//      exclude("*")
      srcDir("src")
    }
//    java {
//      srcDir("src")
//    }
  }
  test {
    kotlin {
      srcDir("test")
    }
//    java {
//      srcDir("test")
//    }
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
  compilerOptions {
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
  }
  testing {
    dependencies {
      implementation("org.jetbrains.kotlin:kotlin-test-junit5")
    }
  }
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

tasks.withType<JavaCompile>().configureEach {
  options.release.set(22)
}
