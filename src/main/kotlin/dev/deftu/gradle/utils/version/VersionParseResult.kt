package dev.deftu.gradle.utils.version

sealed interface VersionParseResult {

    inline fun <reified T : MinecraftVersion> get(): T? {
        return (this as? VersionParseSuccess)?.version as? T
    }

    inline fun <reified T : MinecraftVersion> getOrThrow(): T {
        return when (this) {
            is VersionParseSuccess -> {
                version as? T
                    ?: throw MinecraftVersionParsingException(
                        "Expected ${T::class.java.simpleName}, got ${version::class.java.simpleName}"
                    )
            }

            is VersionParseError -> {
                throw MinecraftVersionParsingException(message)
            }
        }
    }

}

open class VersionParseSuccess(
    open val version: MinecraftVersion
) : VersionParseResult

open class VersionParseError(
    open val message: String
) : VersionParseResult
