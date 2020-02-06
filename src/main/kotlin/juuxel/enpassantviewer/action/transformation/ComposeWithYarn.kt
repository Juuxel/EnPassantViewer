package juuxel.enpassantviewer.action.transformation

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import java.awt.event.ActionEvent
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.swing.AbstractAction
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.ui.input.GameVersionDialog
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

class ComposeWithYarn(
    private val context: ActionContext
) : AbstractAction("Compose with Yarn") {
    override fun actionPerformed(e: ActionEvent?) {
        ProgressDialog.show(context.frame, "Composing with Yarn") {
            step = "Finding Yarn mappings"
            val version = GameVersionDialog(context.frame, context.gameVersion).requestInputFromCache(this) ?: return@show
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
                        ComposeWithTiny(context.mappings).run(reader)
                    }
                }
            }

            step = "Setting mappings"
            context.setMappingsAndVersion(composedMappings, GameVersion(version))
            context.setAsterisk(true)
        }
    }
}
