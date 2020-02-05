package juuxel.enpassantviewer.action.mappings

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.AbstractAction
import javax.swing.JFrame
import juuxel.enpassantviewer.ui.input.GameVersionDialog
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.progress.StepManager
import juuxel.enpassantviewer.ui.status.GameVersion

class OpenMojmap(
    private val frame: JFrame,
    private val gameVersion: () -> GameVersion,
    private val mappingsSetter: (ProjectMapping, GameVersion) -> Unit
) : AbstractAction("Open Mojmap") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Opening mojmap") { run() }
    }

    private fun StepManager.run() {
        val version = GameVersionDialog(frame, gameVersion()).requestInputFromCache(this) ?: return
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
        mappingsSetter(mappings, GameVersion(version))
    }
}
