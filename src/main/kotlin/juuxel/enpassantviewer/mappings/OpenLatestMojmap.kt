package juuxel.enpassantviewer.mappings

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import juuxel.enpassantviewer.ui.ProgressDialog
import juuxel.enpassantviewer.ui.StepManager
import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.AbstractAction
import javax.swing.JFrame

class OpenLatestMojmap(
    private val frame: JFrame,
    private val release: Boolean,
    private val mappingsSetter: (ProjectMapping) -> Unit
) : AbstractAction("Open Latest Mojmap (${if (release) "Release" else "Snapshot"})") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Opening mojmap") { run() }
    }

    private fun StepManager.run() {
        val version = if (release) MappingCache.getLatestRelease(this) else MappingCache.getLatestSnapshot(this)
        val versionManifest = MappingCache.getVersionManifest(this, version)
        val mojmapUrl = versionManifest
            .getObject("downloads")!!
            .getObject("client_mappings")!!
            .get(String::class.java, "url")
            .let { URL(it) }

        step = "Downloading and parsing $release mappings"
        val mappings = mojmapUrl.openStream().use { input ->
            input.reader().useLines { parseProguardMappings(it) }
        }

        step = "Setting the mappings"
        mappingsSetter(mappings)
    }
}
