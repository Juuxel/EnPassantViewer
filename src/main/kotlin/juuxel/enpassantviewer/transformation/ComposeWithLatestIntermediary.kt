package juuxel.enpassantviewer.transformation

import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.mappings.MappingCache
import juuxel.enpassantviewer.ui.ProgressDialog
import java.awt.event.ActionEvent
import java.net.URL
import java.util.zip.InflaterInputStream
import javax.swing.AbstractAction
import javax.swing.JFrame

class ComposeWithLatestIntermediary(
    private val frame: JFrame,
    private val release: Boolean,
    private val mappings: () -> ProjectMapping,
    private val mappingsSetter: (ProjectMapping) -> Unit
) : AbstractAction("Compose with Latest Intermediary (${if (release) "Release" else "Snapshot"})") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Composing with Intermediary") {
            val version = if (release) MappingCache.getLatestRelease(this) else MappingCache.getLatestSnapshot(this)
            val yarnUrl = URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/$version.tiny")
            val composedMappings = yarnUrl.openStream().use { input ->
                input.bufferedReader().use { reader ->
                    ComposeWithTiny(mappings()).run(reader)
                }
            }

            step = "Setting mappings"
            mappingsSetter(composedMappings)
        }
    }
}