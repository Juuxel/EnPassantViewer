package juuxel.enpassantviewer.action

import blue.endless.jankson.Jankson
import blue.endless.jankson.JsonArray
import blue.endless.jankson.JsonObject
import java.awt.event.ActionEvent
import java.io.BufferedReader
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.swing.AbstractAction
import juuxel.enpassantviewer.ui.input.GameVersionDialog

abstract class AbstractYarnAction(name: String, protected val context: ActionContext) : AbstractAction(name) {
    final override fun actionPerformed(e: ActionEvent?) {
        val version = GameVersionDialog(context.frame, context.gameVersion).requestInputFromCache(null) ?: return
        val dataUrl = URL("https://meta.fabricmc.net/v2/versions/yarn/$version")
        val jankson = Jankson.builder().build()
        val mappingData = dataUrl.openStream().use { input -> jankson.loadElement(input) } as JsonArray
        val yarnVersion = mappingData.filterIsInstance<JsonObject>()
            .maxBy { it.getInt("build", -1) }!!
            .get(String::class.java, "version")!!

        val yarnUrl = URL("https://maven.fabricmc.net/net/fabricmc/yarn/$yarnVersion/yarn-$yarnVersion-tiny.gz")
        yarnUrl.openStream().use { input ->
            GZIPInputStream(input).use { inflater ->
                inflater.bufferedReader().use { reader -> run(reader, version) }
            }
        }
    }

    abstract fun run(mappingsReader: BufferedReader, version: String)
}
