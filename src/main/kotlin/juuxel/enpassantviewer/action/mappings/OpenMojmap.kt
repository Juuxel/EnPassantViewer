package juuxel.enpassantviewer.action.mappings

import io.github.cottonmc.proguardparser.parseProguardMappings
import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.AbstractAction
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.ui.input.GameVersionDialog
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.progress.StepManager
import juuxel.enpassantviewer.ui.status.GameVersion

class OpenMojmap(
    private val context: ActionContext
) : AbstractAction("Open Mojmap") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(context.frame, "Opening Mojmap") { run() }
    }

    private fun StepManager.run() {
        val version = GameVersionDialog(context.frame, GameVersion.Unknown).requestInputFromCache(this) ?: return
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
        context.setMappingsAndVersion(mappings, GameVersion(version))
        context.setAsterisk(false)
    }
}
