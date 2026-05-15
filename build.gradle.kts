import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm") version("2.3.20")
    `kotlin-dsl`
    val dgtVersion = "2.73.0"
    id("dev.deftu.gradle.tools.repo") version(dgtVersion)
    id("dev.deftu.gradle.tools.configure") version(dgtVersion)
}

tasks.withType<JavaCompile> {
  options.release = 25
}

tasks.withType<KotlinJvmCompile> {
  compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
}

toolkitMavenPublishing {
    setupPublication.set(false)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TheDGOfficial/Gradle-Toolkit")

            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")).toString()
                password = (project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")).toString()
            }
        }
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()

    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.architectury.dev/")
    maven("https://jitpack.io/")
}

dependencies {
    // Language
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlin.version")}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${property("kotlin.version")}")

    // Architectury Loom
    implementation("gg.essential:architectury-loom:1.15.50")
    implementation("dev.architectury:architectury-pack200:0.1.3")

    // Preprocessing/multi-versioning
    implementation("dev.deftu:preprocessor:0.16.0")

    // Documentation
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
    implementation("org.jetbrains.dokka:dokka-base:2.0.0")

    // Publishing
    implementation("com.modrinth.minotaur:Minotaur:2.8.7")
    implementation("net.darkhax.curseforgegradle:CurseForgeGradle:1.1.26")
    implementation("com.github.breadmoirai:github-release:2.5.2")

    // Other
    implementation("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.4.0")
    implementation("dev.deftu:Bloom:0.2.0")
}

tasks {
    named<Jar>("jar") {
        from("LICENSE")
    }
}
