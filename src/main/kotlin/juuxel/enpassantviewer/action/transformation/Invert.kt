package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.classes

object Invert {
    fun run(mappings: ProjectMapping): ProjectMapping =
        ProjectMapping.classes.modify(mappings) { classes ->
            classes.map { c ->
                c.copy(
                    from = c.to,
                    to = c.from,
                    // TODO: Invert member descriptors?
                    fields = c.fields.map { f ->
                        f.copy(from = f.to, to = f.from)
                    },
                    methods = c.methods.map { m ->
                        m.copy(from = m.to, to = m.from)
                    }
                )
            }
        }
}
