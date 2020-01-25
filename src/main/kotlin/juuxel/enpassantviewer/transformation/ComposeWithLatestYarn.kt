package juuxel.enpassantviewer.transformation

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.mappings.MappingCache
import juuxel.enpassantviewer.ui.ProgressDialog
import java.awt.event.ActionEvent
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.swing.AbstractAction
import javax.swing.JFrame

class ComposeWithLatestYarn(
    private val frame: JFrame,
    private val release: Boolean,
    private val mappings: () -> ProjectMapping,
    private val mappingsSetter: (ProjectMapping) -> Unit
) : AbstractAction("Compose with Latest Yarn (${if (release) "Release" else "Snapshot"})") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(frame, "Composing with Yarn") {
            step = "Finding latest Yarn mappings"
            val version = if (release) MappingCache.getLatestRelease(this) else MappingCache.getLatestSnapshot(this)
            val dataUrl = URL("https://meta.fabricmc.net/v1/versions/mappings/$version")
            val jankson = Jankson.builder().build()
            val mappingData = dataUrl.openStream().use { input ->
                input.reader().use { reader ->
                    jankson.load("""{"value":${reader.readText()}}""")
                }
            }.get("value") as JsonArray
            val yarnVersion = mappingData.filterIsInstance<JsonObject>()
                .find { it.getBoolean("stable", false) }!!
                .get(String::class.java, "version")!!

            step = "Composing with latest Yarn mappings"
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