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
    api(libs.adventure.api)
    api(libs.adventure.minimessage)
    api(libs.adventure.plain)
    api(libs.adventure.gson)
    api(libs.adventure.legacy)
}
