package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.classes
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import juuxel.enpassantviewer.action.ActionContext

class DuplicateOrigin(private val context: ActionContext) : AbstractAction("Duplicate Left Side") {
    override fun actionPerformed(e: ActionEvent?) {
        val newMappings = ProjectMapping.classes.modify(context.mappings) { classes ->
            classes.map { c ->
                c.copy(
                    from = c.from, to = c.from,
                    fields = c.fields.map { f -> f.copy(from = f.from, to = f.from) },
                    methods = c.methods.map { m -> m.copy(from = m.from, to = m.from) }
                )
            }
        }

        context.setMappings(newMappings)
        context.setAsterisk(true)
    }
}
