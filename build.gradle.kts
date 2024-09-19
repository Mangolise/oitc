plugins {
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version("8.1.7")
}

var versionStr = System.getenv("GIT_COMMIT") ?: "dev"

group = "net.mangolise"
version = versionStr

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.serble.net/snapshots/")
}

dependencies {
    implementation("net.mangolise:mango-game-sdk:latest")
    implementation("net.minestom:minestom-snapshots:6c5cd6544e")
}

java {
    withSourcesJar()
}

tasks.register("packageWorlds", net.mangolise.gamesdk.gradle.PackageWorldTask::class.java)
tasks.processResources {
    dependsOn("packageWorlds")
}

publishing {
    repositories {
        maven {
            name = "serbleMaven"
            url = uri("https://maven.serble.net/snapshots/")
            credentials {
                username = System.getenv("SERBLE_REPO_USERNAME") ?: ""
                password = System.getenv("SERBLE_REPO_PASSWORD") ?: ""
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenGitCommit") {
            groupId = "net.mangolise"
            artifactId = "oitc"
            version = versionStr
            from(components["java"])
        }

        create<MavenPublication>("mavenLatest") {
            groupId = "net.mangolise"
            artifactId = "oitc"
            version = "latest"
            from(components["java"])
        }
    }
}

tasks.withType<Jar> {
    manifest {
        // Change this to your main class
        attributes["Main-Class"] = "net.mangolise.paintball.Test"
    }
}
