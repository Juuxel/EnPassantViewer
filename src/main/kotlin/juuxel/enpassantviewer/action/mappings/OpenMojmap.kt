package juuxel.enpassantviewer.action.mappings

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import juuxel.enpassantviewer.ui.MappingVersionDialog
import juuxel.enpassantviewer.ui.ProgressDialog
import juuxel.enpassantviewer.ui.StepManager
import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.AbstractAction
import javax.swing.JFrame

class OpenMojmap(
    private val frame: JFrame,
    private val mappingsSetter: (ProjectMapping) -> Unit
) : AbstractAction("Open Mojmap") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Opening mojmap") { run() }
    }

    private fun StepManager.run() {
        val version = when (val version = MappingVersionDialog(frame).requestInput()) {
            MappingVersionDialog.Result.LatestRelease -> MappingCache.getLatestRelease(this)
            MappingVersionDialog.Result.LatestSnapshot -> MappingCache.getLatestSnapshot(this)
            is MappingVersionDialog.Result.Custom -> version.version
            MappingVersionDialog.Result.Cancelled -> return
        }
        val versionManifest = MappingCache.getVersionManifest(this, version)
        val mojmapUrl = versionManifest
            .getObject("downloads")!!
            .getObject("client_mappings")!!
            .get(String::class.java, "url")
            .let { URL(it) }

        step = "Downloading and parsing $version mappings"
        val mappings = mojmapUrl.openStream().use { input ->
            input.reader().useLines { parseProguardMappings(it) }
        }

        step = "Setting the mappings"
        mappingsSetter(mappings)
    }
}
