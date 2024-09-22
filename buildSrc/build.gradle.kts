plugins {
    id("java")
}

group = "net.mangolise"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    implementation("net.mangolise:mango-game-sdk:latest")
    implementation("net.minestom:minestom-snapshots:d0754f2a15")
    implementation("dev.hollowcube:polar:1.11.1")
    implementation(gradleApi())
}
