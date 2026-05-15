package dev.deftu.gradle.tools.minecraft

import dev.deftu.gradle.ToolkitConstants
import dev.deftu.gradle.utils.MCData
import dev.deftu.gradle.utils.withLoom
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

abstract class ApiExtension(
    val project: Project
) {
    companion object {
        const val SOURCE_SET_NAME = "testMod"
        const val CLIENT_RUN_NAME = "testClient"
        const val SERVER_RUN_NAME = "testServer"
    }

    private inline val mcData: MCData
        get() = MCData.from(project)

    private inline val sourceSets
        get() = project.extensions.getByType<JavaPluginExtension>().sourceSets

    private inline val sourceSet: SourceSet?
        get() = sourceSets.findByName(SOURCE_SET_NAME)

    fun setupTestSourceSet() {
        if (null != sourceSet) {
            return
        }

        val mainSourceSet = sourceSets.getByName("main")

        sourceSets.create(SOURCE_SET_NAME) {
            compileClasspath += mainSourceSet.compileClasspath
            runtimeClasspath += mainSourceSet.runtimeClasspath
        }

        project.dependencies {
            add("${SOURCE_SET_NAME}Implementation", mainSourceSet.output)
            add("${SOURCE_SET_NAME}RuntimeOnly", mainSourceSet.output)
            add("${SOURCE_SET_NAME}CompileOnly", mainSourceSet.output)
        }
    }

    fun setupTestJar() {
        val currentSourceSet = sourceSet
            ?: throw IllegalStateException("Test source set not found")

        val devLibsDir = project.layout.buildDirectory.dir("dev-libs")

        val testJar = project.tasks.register<Jar>("testJar") {
            group = ToolkitConstants.TASK_GROUP

            archiveClassifier.set("test-mod-dev")
            destinationDirectory.set(devLibsDir)
            from(currentSourceSet.output)
        }.get()

        if (!mcData.version.isDrop) {
            val remapTestJar = project.tasks.register<RemapJarTask>("remapTestJar") {
                group = ToolkitConstants.TASK_GROUP

                archiveClassifier.set("test-mod")
                destinationDirectory.set(devLibsDir)
                inputFile.set(testJar.archiveFile)
                classpath.setFrom(currentSourceSet.compileClasspath)
            }.get()

            project.tasks.named("build") {
                dependsOn(remapTestJar)
            }
        } else {
            project.tasks.named("build") {
                dependsOn(testJar)
            }
        }
    }

    fun setupTestClient() {
        setupTestSourceSet()
        setupTestJar()

        val currentSourceSet = sourceSet
            ?: throw IllegalStateException("Test source set not found")

        project.withLoom {
            runs {
                create(CLIENT_RUN_NAME) {
                    client()
                    source(currentSourceSet)

                    val configuration = project.configurations.create(CLIENT_RUN_NAME)

                    if (!mcData.isFabric) {
                        mods {
                            create(SOURCE_SET_NAME) {
                                source(currentSourceSet)
                                configuration(configuration)
                            }
                        }
                    }
                }
            }
        }
    }

    fun setupTestServer() {
        setupTestSourceSet()
        setupTestJar()

        val currentSourceSet = sourceSet
            ?: throw IllegalStateException("Test source set not found")

        project.withLoom {
            runs {
                create(SERVER_RUN_NAME) {
                    server()
                    source(currentSourceSet)

                    val configuration = project.configurations.create(SERVER_RUN_NAME)

                    if (!mcData.isFabric) {
                        mods {
                            create(SOURCE_SET_NAME) {
                                source(currentSourceSet)
                                configuration(configuration)
                            }
                        }
                    }
                }
            }
        }
    }

    fun setupTestWorkspace() {
        setupTestClient()
        setupTestServer()
    }
}
