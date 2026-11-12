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
    api(project(":common"))
    compileOnly(libs.paper.legacy)
    api(libs.adventure.bukkit)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vault)
}
