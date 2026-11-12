plugins {
    java
    id("com.gradleup.shadow") version "9.2.2" apply false
}

allprojects {
    group = "io.github.blazevortex"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigot" }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") { name = "sonatype-snapshots" }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") { name = "placeholderapi" }
        maven("https://jitpack.io") { name = "jitpack" }
        maven("https://maven.enginehub.org/repo/") { name = "enginehub" }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    the<JavaPluginExtension>().withSourcesJar()

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.3")
        "testImplementation"("org.assertj:assertj-core:3.26.3")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }
}

tasks.register("printArtifacts") {
    group = "help"
    doLast {
        println("Blaze Vortex artifacts:")
        println("  plugin-legacy -> BlazeVortex-legacy-${version}.jar  (Bukkit/Spigot/Paper 1.19.x-1.21.11)")
        println("  plugin-paper  -> BlazeVortex-paper-${version}.jar   (Paper 26.1-26.3, Java 25)")
    }
}
