package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.classes

object Invert {
    private fun tryConvertToTarget(mappings: ProjectMapping, type: String) =
        mappings.findClassOrNull(type)?.to ?: type

    fun run(mappings: ProjectMapping): ProjectMapping =
        ProjectMapping.classes.modify(mappings) { classes ->
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
}
