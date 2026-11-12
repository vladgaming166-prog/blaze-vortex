plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

dependencies {
    api(project(":platform-bukkit"))
    compileOnly(libs.paper.modern)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vault)
}
