package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.classes
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.ui.progress.ProgressDialog

class Invert(private val context: ActionContext) : AbstractAction("Invert") {
    private fun tryConvertToTarget(mappings: ProjectMapping, type: String): String {
        // FIXME: Isolate this
        val colonPart = if (':' in type) type.substringBeforeLast(':') + ':' else ""
        val nonArrayPart = type.substringAfterLast(':').substringBefore('[')
        val arrayPart = if ('[' in type) type.substringAfter('[') + '[' else ""
        return mappings.findClassOrNull(nonArrayPart)?.to?.let { name ->
            "$colonPart$name$arrayPart"
        } ?: type
    }

    override fun actionPerformed(e: ActionEvent?) = ProgressDialog.show(context.frame, "Inverting mappings") {
        val mappings = context.mappings
        val newMappings = ProjectMapping.classes.modify(mappings) { classes ->
            classes.map { c ->
                c.copy(
                    from = c.to,
                    to = c.from,
                    fields = c.fields.map { f ->
                        f.copy(
                            from = f.to,
                            to = f.from,
                            type = tryConvertToTarget(mappings, f.type)
                        )
                    },
                    methods = c.methods.map { m ->
                        m.copy(
                            from = m.to,
                            to = m.from,
                            returnType = tryConvertToTarget(mappings, m.returnType),
                            parameters = m.parameters.map { tryConvertToTarget(mappings, it) }
                        )
                    }
                )
            }
        }

        context.setMappings(newMappings)
        context.setAsterisk(true)
    }
}
