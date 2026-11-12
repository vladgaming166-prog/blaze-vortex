plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    api(project(":api"))
    api(project(":platform-api"))
    api(libs.gson)
    api(libs.snakeyaml)
    api(libs.imageio.core)
    api(libs.imageio.webp)
    api(libs.imageio.jpeg)
}
