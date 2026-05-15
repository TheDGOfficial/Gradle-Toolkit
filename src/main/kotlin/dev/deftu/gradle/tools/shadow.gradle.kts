package dev.deftu.gradle.tools

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.deftu.gradle.ToolkitConstants
import dev.deftu.gradle.utils.MCData
import dev.deftu.gradle.utils.ModData
import dev.deftu.gradle.utils.withLoom
import dev.deftu.gradle.utils.withLoomPlugin
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

plugins {
    java
}

val shade: Configuration by configurations.creating { }

val fatJar = tasks.register<ShadowJar>("fatJar") {
    group = ToolkitConstants.TASK_GROUP
    description = "Builds a fat JAR with all dependencies shaded in"

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations.add(shade)

    archiveVersion.set(project.version.toString())
    archiveClassifier.set("all")

    val javaPlugin = project.extensions.getByType(JavaPluginExtension::class.java)
    val jarTask = project.tasks.getByName("jar") as Jar

    manifest.from(jarTask.manifest)

    val libsProvider = project.provider {
        listOf(jarTask.manifest.attributes["Class-Path"])
    }

    val files = project.objects.fileCollection().from(shade)

    doFirst {
        if (!files.isEmpty) {
            val libs = libsProvider.get().toMutableList()

            libs.addAll(files.map { it.name })

            manifest.attributes(
                mapOf(
                    "Class-Path" to libs.filterNotNull().joinToString(" ")
                )
            )
        }
    }

    from(javaPlugin.sourceSets.getByName("main").output)

    exclude(
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "module-info.class"
    )
}

project.artifacts.add("shade", fatJar)

pluginManager.withPlugin("java") {
    tasks.named("assemble") {
        dependsOn(fatJar)
    }
}

pluginManager.withLoomPlugin {
    val shadeNonTransitive: Configuration by configurations.creating {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }

    fatJar.configure {
        configurations.add(shadeNonTransitive)
    }
}

tasks {
    val shadowJar = findByName("shadowJar")

    if (null != shadowJar) {
        named("shadowJar") {
            doFirst {
                throw GradleException("Incorrect task! You're looking for fatJar.")
            }
        }
    }
}

withLoom {
    val mcData = MCData.from(project)

    if (mcData.version.isDrop) {
        fatJar.configure {
            archiveClassifier.set("")

            val modData = ModData.from(project)

            archiveBaseName.set(modData.name)
        }
    } else {
        fatJar.configure {
            archiveClassifier.set("dev")
        }

        tasks.named<RemapJarTask>("remapJar") {
            dependsOn(fatJar)

            inputFile.set(fatJar.flatMap { it.archiveFile })

            archiveClassifier.set("")

            val modData = ModData.from(project)

            archiveBaseName.set(modData.name)
        }
    }
}
