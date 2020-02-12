package juuxel.enpassantviewer.action.mappings

import io.github.cottonmc.proguardparser.ProjectMapping
import java.awt.event.ActionEvent
import java.io.BufferedReader
import javax.swing.AbstractAction
import javax.swing.JFileChooser
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.format.TinyConverter
import juuxel.enpassantviewer.ui.input.InputDialog
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.progress.StepManager
import juuxel.enpassantviewer.ui.status.GameVersion
import net.fabricmc.mapping.tree.TinyMappingFactory

class OpenTiny(private val context: ActionContext) : AbstractAction("Open Tiny") {
    override fun actionPerformed(e: ActionEvent?) {
        val chooser = context.fileChooser
        val chooserResult = chooser.showOpenDialog(context.frame)
        if (chooserResult != JFileChooser.APPROVE_OPTION) return

        chooser.selectedFile.bufferedReader().use { tinyReader ->
            ProgressDialog.show(context.frame, "Opening Tiny Mappings") {
                step = "Converting mappings"
                val mappings = run(tinyReader)

                step = "Setting mappings"
                context.setMappingsAndVersion(mappings, GameVersion.Unknown)
                context.setAsterisk(true)
            }
        }
    }

    fun StepManager.run(tinyReader: BufferedReader): ProjectMapping {
        val tree = TinyMappingFactory.loadWithDetection(tinyReader)
        val namespaces = tree.metadata.namespaces
        val defaultInputNamespace = namespaces.first()
        val defaultTargetNamespace = namespaces.last()

        val (inputNamespace, targetNamespace) = InputDialog.requestTinyNamespaces(
            defaultInputNamespace, defaultTargetNamespace
        )

        pushStep("Converting mappings")
        val mappings = TinyConverter(inputNamespace, targetNamespace).toProguard(tree)
        popStep()
        return mappings
    }
}
