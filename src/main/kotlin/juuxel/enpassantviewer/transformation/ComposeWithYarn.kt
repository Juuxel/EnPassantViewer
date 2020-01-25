package juuxel.enpassantviewer.transformation

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.mappings.MappingCache
import juuxel.enpassantviewer.ui.MappingVersionDialog
import juuxel.enpassantviewer.ui.ProgressDialog
import java.awt.event.ActionEvent
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.swing.AbstractAction
import javax.swing.JFrame

class ComposeWithYarn(
    private val frame: JFrame,
    private val mappings: () -> ProjectMapping,
    private val mappingsSetter: (ProjectMapping) -> Unit
) : AbstractAction("Compose with Yarn") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Composing with Yarn") {
            step = "Finding Yarn mappings"
            val version = when (val version = MappingVersionDialog(frame).requestInput()) {
                MappingVersionDialog.Result.LatestRelease -> MappingCache.getLatestRelease(this)
                MappingVersionDialog.Result.LatestSnapshot -> MappingCache.getLatestSnapshot(this)
                is MappingVersionDialog.Result.Custom -> version.version
                MappingVersionDialog.Result.Cancelled -> return@show
            }
            val dataUrl = URL("https://meta.fabricmc.net/v1/versions/mappings/$version")
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
            mappingsSetter(composedMappings)
        }
    }
}