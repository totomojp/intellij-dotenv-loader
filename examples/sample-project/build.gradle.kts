plugins {
    kotlin("jvm") version "2.1.0"
    application
}

group = "com.example.sample"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "com.example.sample.MainKt"
}
