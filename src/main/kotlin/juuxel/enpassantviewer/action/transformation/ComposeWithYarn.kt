package juuxel.enpassantviewer.action.transformation

import java.io.BufferedReader
import juuxel.enpassantviewer.action.AbstractYarnAction
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

class ComposeWithYarn(context: ActionContext) : AbstractYarnAction("Compose with Yarn", context) {
    override fun run(mappingsReader: BufferedReader, version: String) =
        ProgressDialog.show(context.frame, "Composing with Yarn") {
            step = "Composing with Yarn mappings"
            val composedMappings = ComposeWithTiny(context.mappings).run(mappingsReader)

            step = "Setting mappings"
            context.setMappingsAndVersion(composedMappings, GameVersion(version))
            context.setAsterisk(true)
        }
}
