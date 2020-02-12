package juuxel.enpassantviewer.format

import io.github.cottonmc.proguardparser.ClassMapping
import io.github.cottonmc.proguardparser.FieldMapping
import io.github.cottonmc.proguardparser.MethodMapping
import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.descriptor.Descriptors
import juuxel.enpassantviewer.descriptor.MethodDescriptor
import net.fabricmc.mapping.tree.TinyTree

class TinyConverter(private val fromNamespace: String, private val toNamespace: String) : Converter<TinyTree> {
    override fun toProguard(mappings: TinyTree): ProjectMapping {
        return ProjectMapping(mappings.classes.map { c ->
            val fields = c.fields.map { f ->
                FieldMapping(
                    type = Descriptors.descriptorToReadable(f.getDescriptor(fromNamespace)),
                    from = f.getName(fromNamespace),
                    to = f.getName(toNamespace)
                )
            }

            val methods = c.methods.map { m ->
                val desc = MethodDescriptor.fromDescriptor(m.getDescriptor(fromNamespace))
                MethodMapping(
                    returnType = desc.returnType,
                    from = m.getName(fromNamespace),
                    to = m.getName(toNamespace),
                    parameters = desc.parameters
                )
            }

            ClassMapping(
                c.getName(fromNamespace).replace('/', '.'),
                c.getName(toNamespace).replace('/', '.'),
                fields,
                methods
            )
        })
    }

    companion object {
        val DEFAULT: TinyConverter = TinyConverter("official", "named")
        val INTERMEDIARY: TinyConverter = TinyConverter("official", "intermediary")
    }
}
