package juuxel.enpassantviewer.action.transformation

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import io.github.cottonmc.proguardparser.ProjectMapping
import java.awt.event.ActionEvent
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.swing.AbstractAction
import javax.swing.JFrame
import juuxel.enpassantviewer.action.mappings.MappingCache
import juuxel.enpassantviewer.ui.MappingVersionDialog
import juuxel.enpassantviewer.ui.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

class ComposeWithYarn(
    private val frame: JFrame,
    private val mappings: () -> ProjectMapping,
    private val gameVersion: () -> GameVersion,
    private val mappingsSetter: (ProjectMapping, GameVersion) -> Unit
) : AbstractAction("Compose with Yarn") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Composing with Yarn") {
            step = "Finding Yarn mappings"
            val version = when (val version = MappingVersionDialog(frame, gameVersion().getVersionOrNull()).requestInput()) {
                MappingVersionDialog.Result.LatestRelease -> MappingCache.getLatestRelease(this)
                MappingVersionDialog.Result.LatestSnapshot -> MappingCache.getLatestSnapshot(this)
                is MappingVersionDialog.Result.Custom -> version.version
                MappingVersionDialog.Result.Cancelled -> return@show
            }
            val dataUrl = URL("https://meta.fabricmc.net/v2/versions/yarn/$version")
            val jankson = Jankson.builder().build()
            val mappingData = dataUrl.openStream().use { input -> jankson.loadElement(input) } as JsonArray
            val yarnVersion = mappingData.filterIsInstance<JsonObject>()
                .maxBy { it.getInt("build", -1) }!!
                .get(String::class.java, "version")!!

            step = "Composing with Yarn mappings"
            val yarnUrl = URL("https://maven.fabricmc.net/net/fabricmc/yarn/$yarnVersion/yarn-$yarnVersion-tiny.gz")
            val composedMappings = yarnUrl.openStream().use { input ->
                GZIPInputStream(input).use { inflater ->
                    inflater.bufferedReader().use { reader ->
                        ComposeWithTiny(mappings()).run(reader)
                    }
                }
            }

            step = "Setting mappings"
            mappingsSetter(composedMappings, GameVersion(version))
        }
    }
}
