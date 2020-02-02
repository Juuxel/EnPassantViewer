package juuxel.enpassantviewer.action.mappings

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import java.net.URL
import juuxel.enpassantviewer.ui.StepManager

object MappingCache {
    private lateinit var manifest: JsonObject
    private lateinit var latestRelease: String
    private lateinit var latestSnapshot: String

    private val versionManifests: MutableMap<String, JsonObject> = HashMap()

    fun getManifest(stepManager: StepManager?): JsonObject {
        if (!::manifest.isInitialized) {
            stepManager?.pushStep("Downloading full version manifest")
            val manifestUrl = URL("https://launchermeta.mojang.com/mc/game/version_manifest.json")
            val jankson = Jankson.builder().build()
            manifest = manifestUrl.openStream().use { input -> jankson.load(input) }
            stepManager?.popStep()
        }

        return manifest
    }

    fun getLatestRelease(stepManager: StepManager?): String {
        if (!::latestRelease.isInitialized) {
            stepManager?.pushStep("Finding latest release")
            val manifest = getManifest(stepManager)
            latestRelease = manifest.getObject("latest")!!.get(String::class.java, "release")!!
            stepManager?.popStep()
        }

        return latestRelease
    }

    fun getLatestSnapshot(stepManager: StepManager?): String {
        if (!::latestSnapshot.isInitialized) {
            stepManager?.pushStep("Finding latest snapshot")
            val manifest = getManifest(stepManager)
            latestSnapshot = manifest.getObject("latest")!!.get(String::class.java, "snapshot")!!
            stepManager?.popStep()
        }

        return latestSnapshot
    }

    fun getVersionManifest(stepManager: StepManager?, version: String): JsonObject {
        return versionManifests.getOrPut(version) {
            stepManager?.pushStep("Downloading version manifest for $version")
            val jankson = Jankson.builder().build()
            val versionObject = getManifest(stepManager).get(JsonArray::class.java, "versions")!!.find {
                it is JsonObject && it[String::class.java, "id"] == version
            } as JsonObject
            val versionManifestUrl = URL(versionObject[String::class.java, "url"])
            val result = versionManifestUrl.openStream().use { input -> jankson.load(input) }
            stepManager?.popStep()
            result
        }
    }
}
