@file:JvmName("LibraryHelper")

package dev.deftu.gradle.utils

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope

// Kotlin DSL
fun DependencyHandlerScope.setupJetBrainsExposed(
    version: String,
    apply: (String) -> Unit = { dependency ->
        add("implementation", dependency)
    },
    vararg modules: String
) {
    for (module in modules) {
        apply("org.jetbrains.exposed:exposed-$module:$version")
    }
}

// Gradle Groovy
fun setupJetBrainsExposed(
    dependencies: DependencyHandler,
    version: String,
    apply: (String) -> Unit = { dependency ->
        dependencies.add("implementation", dependency)
    },
    vararg modules: String
) {
    for (module in modules) {
        apply("org.jetbrains.exposed:exposed-$module:$version")
    }
}
