package juuxel.enpassantviewer.action.transformation

import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.AbstractAction
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.ui.input.GameVersionDialog
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

class ComposeWithIntermediary(
    private val context: ActionContext
) : AbstractAction("Compose with Intermediary") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(context.frame, "Composing with Intermediary") {
            val version = GameVersionDialog(context.frame, context.gameVersion).requestInputFromCache(this) ?: return@show
            val yarnUrl = URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/$version.tiny")
            val composedMappings = yarnUrl.openStream().use { input ->
                input.bufferedReader().use { reader ->
                    ComposeWithTiny(context.mappings).run(reader)
                }
            }

            step = "Setting mappings"
            context.setMappingsAndVersion(composedMappings, GameVersion(version))
            context.setAsterisk(true)
        }
    }
}
