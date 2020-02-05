package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.ProjectMapping
import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.AbstractAction
import javax.swing.JFrame
import juuxel.enpassantviewer.action.mappings.MappingCache
import juuxel.enpassantviewer.ui.input.GameVersionDialog
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

class ComposeWithIntermediary(
    private val frame: JFrame,
    private val mappings: () -> ProjectMapping,
    private val gameVersion: () -> GameVersion,
    private val mappingsSetter: (ProjectMapping, GameVersion) -> Unit
) : AbstractAction("Compose with Intermediary") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Composing with Intermediary") {
            val version = when (val version = GameVersionDialog(frame, gameVersion().getVersionOrNull()).requestInput()) {
                GameVersionDialog.Result.LatestRelease -> MappingCache.getLatestRelease(this)
                GameVersionDialog.Result.LatestSnapshot -> MappingCache.getLatestSnapshot(this)
                is GameVersionDialog.Result.Custom -> version.version
                GameVersionDialog.Result.Cancelled -> return@show
            }
            val yarnUrl = URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/$version.tiny")
            val composedMappings = yarnUrl.openStream().use { input ->
                input.bufferedReader().use { reader ->
                    ComposeWithTiny(mappings()).run(reader)
                }
            }

            step = "Setting mappings"
            mappingsSetter(composedMappings, GameVersion(version))
        }
    }
}
