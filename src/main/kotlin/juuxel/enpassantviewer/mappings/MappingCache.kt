package juuxel.enpassantviewer.mappings

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import juuxel.enpassantviewer.ui.StepManager
import java.net.URL

object MappingCache {
    private lateinit var manifest: JsonObject
    private lateinit var latestRelease: String
    private lateinit var latestSnapshot: String

    private val versionManifests: MutableMap<String, JsonObject> = HashMap()

    fun getManifest(stepManager: StepManager?): JsonObject {
        if (!::manifest.isInitialized) {
            stepManager?.step = "Downloading full version manifest"
            val manifestUrl = URL("https://launchermeta.mojang.com/mc/game/version_manifest.json")
            val jankson = Jankson.builder().build()
            manifest = manifestUrl.openStream().use { input -> jankson.load(input) }
        }

        return manifest
    }

    fun getLatestRelease(stepManager: StepManager?): String {
        if (!::latestRelease.isInitialized) {
            stepManager?.step = "Finding latest release"
            val manifest = getManifest(stepManager)
            latestRelease = manifest.getObject("latest")!!.get(String::class.java, "release")!!
        }

        return latestRelease
    }

    fun getLatestSnapshot(stepManager: StepManager?): String {
        if (!::latestSnapshot.isInitialized) {
            stepManager?.step = "Finding latest snapshot"
            val manifest = getManifest(stepManager)
            latestSnapshot = manifest.getObject("latest")!!.get(String::class.java, "snapshot")!!
        }

        return latestSnapshot
    }

    fun getVersionManifest(stepManager: StepManager?, version: String): JsonObject {
        return versionManifests.getOrPut(version) {
            stepManager?.step = "Downloading version manifest for $version"
            val jankson = Jankson.builder().build()
            val versionObject = getManifest(stepManager).get(JsonArray::class.java, "versions")!!.find {
                it is JsonObject && it[String::class.java, "id"] == version
            } as JsonObject
            val versionManifestUrl = URL(versionObject[String::class.java, "url"])
            versionManifestUrl.openStream().use { input -> jankson.load(input) }
        }
    }
}
