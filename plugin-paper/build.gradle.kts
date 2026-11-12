plugins {
    `java-library`
    alias(libs.plugins.shadow)
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
    implementation(project(":platform-paper"))
    compileOnly(libs.paper.modern)
}

tasks.processResources {
    val pluginVersion = project.version.toString()
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand("version" to pluginVersion)
    }
}

tasks.shadowJar {
    archiveBaseName.set("BlazeVortex-paper")
    archiveClassifier.set("")
    mergeServiceFiles()
    relocate("com.google.gson", "io.github.blazevortex.libs.gson")
    relocate("org.yaml.snakeyaml", "io.github.blazevortex.libs.yaml")
    relocate("com.twelvemonkeys", "io.github.blazevortex.libs.twelvemonkeys")
    exclude("net/kyori/**")
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
