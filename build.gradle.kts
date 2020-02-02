plugins {
    java
    kotlin("jvm") version "1.3.61"
    id("org.jmailen.kotlinter") version "2.3.0"
    application
}

group = "io.github.juuxel"
version = "1.0.0"

java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven {
        name = "Cotton"
        url = uri("http://server.bbkr.space:8081/artifactory/libs-release")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net")
    }
    maven {
        name = "Elytra"
        url = uri("https://repo.elytradev.com")
    }
}

application {
    mainClassName = "juuxel.enpassantviewer.MainKt"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.github.cottonmc:proguard-mappings-parser:1.5.0")
    implementation("net.fabricmc:tiny-mappings-parser:0.2.1.13")
    implementation("com.google.guava:guava:28.2-jre")
    implementation("blue.endless:jankson:1.2.0-62")

    implementation("org.swinglabs.swingx:swingx-all:1.6.5-1")
    implementation("com.weblookandfeel:weblaf-ui:1.2.12")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.3.3")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
