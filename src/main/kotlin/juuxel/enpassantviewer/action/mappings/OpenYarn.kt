package juuxel.enpassantviewer.action.mappings

import java.io.BufferedReader
import juuxel.enpassantviewer.action.AbstractYarnAction
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

class OpenYarn(context: ActionContext) : AbstractYarnAction("Open Yarn", context) {
    override fun run(mappingsReader: BufferedReader, version: String) =
        ProgressDialog.show(context.frame, "Opening Yarn") {
            val mappings = with(OpenTiny(context)) { run(mappingsReader) }

            step = "Setting mappings"
            context.setMappingsAndVersion(mappings, GameVersion(version))
            context.setAsterisk(true)
        }
}
