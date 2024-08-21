plugins {
    id("java")
    id("io.github.goooler.shadow") version("8.1.7")
}

group = "net.mangolise"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    implementation("net.mangolise:mango-game-sdk:latest")
    implementation("net.minestom:minestom-snapshots:6c5cd6544e")
}

tasks.withType<Jar> {
    manifest {
        // Change this to your main class
        attributes["Main-Class"] = "net.mangolise.paintball.Test"
    }
}
