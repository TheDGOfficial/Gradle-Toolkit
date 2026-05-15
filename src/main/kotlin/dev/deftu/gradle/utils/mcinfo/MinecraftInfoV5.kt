package dev.deftu.gradle.utils.mcinfo

import dev.deftu.gradle.utils.version.MinecraftVersions

class MinecraftInfoV5 : MinecraftInfo() {
    override fun initialize() {
        inherit(MinecraftInfoV4())

        this.fabricLoaderVersion = "0.18.4"
        this.fabricLanguageKotlinVersion = "1.13.8+kotlin.2.3.0"

        this.fabricApiVersions.putAll(listOf(
            MinecraftVersions.VERSION_26_1 to "0.145.1+26.1",
        ))
    }
}
