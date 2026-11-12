pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "blaze-vortex"

include(
    "api",
    "common",
    "platform-api",
    "platform-bukkit",
    "platform-paper",
    "plugin-legacy",
    "plugin-paper",
    "example"
)
