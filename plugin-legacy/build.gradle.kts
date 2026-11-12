plugins {
    `java-library`
    alias(libs.plugins.shadow)
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
    implementation(project(":platform-bukkit"))
    compileOnly(libs.paper.legacy)
}

tasks.processResources {
    val pluginVersion = project.version.toString()
    filesMatching(listOf("plugin.yml")) {
        expand("version" to pluginVersion)
    }
}

tasks.shadowJar {
    archiveBaseName.set("BlazeVortex-legacy")
    archiveClassifier.set("")
    mergeServiceFiles()
    relocate("com.google.gson", "io.github.blazevortex.libs.gson")
    relocate("org.yaml.snakeyaml", "io.github.blazevortex.libs.yaml")
    relocate("com.twelvemonkeys", "io.github.blazevortex.libs.twelvemonkeys")
    relocate("net.kyori", "io.github.blazevortex.libs.kyori")
    exclude("META-INF/maven/**")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

artifacts {
    archives(tasks.shadowJar)
}
