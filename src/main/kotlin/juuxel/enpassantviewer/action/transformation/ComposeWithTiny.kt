package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.FieldMapping
import io.github.cottonmc.proguardparser.MethodMapping
import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.classes
import java.io.BufferedReader
import juuxel.enpassantviewer.action.mappings.tryConvertToTarget
import juuxel.enpassantviewer.descriptor.Descriptors
import juuxel.enpassantviewer.descriptor.MethodDescriptor
import juuxel.enpassantviewer.ui.input.InputDialog
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.TinyMappingFactory

class ComposeWithTiny(private val mappings: ProjectMapping) {
    fun run(tinyReader: BufferedReader): ProjectMapping {
        val tree = TinyMappingFactory.loadWithDetection(tinyReader)
        val namespaces = tree.metadata.namespaces
        val defaultInputNamespace = namespaces.first()
        val defaultTargetNamespace = namespaces.last()

        val (inputNamespace, targetNamespace) = InputDialog.requestTinyNamespaces(
            defaultInputNamespace, defaultTargetNamespace
        )

        return ProjectMapping.classes.modify(mappings) { classes ->
            classes.map { c ->
                val def = tree.defaultNamespaceClassMap[c.to.replace('.', '/')]
                    ?: return@map c
                c.copy(
                    to = def.getName(targetNamespace).replace('/', '.'),
                    fields = c.fields.map { renameField(it, inputNamespace, targetNamespace, def) },
                    methods = c.methods.map { renameMethod(it, inputNamespace, targetNamespace, def) }
                )
            }
        }
    }

    private fun FieldMapping.getDescriptor() = Descriptors.readableToDescriptor(tryConvertToTarget(mappings, type))

    private fun MethodMapping.getDescriptor() =
        MethodDescriptor(
            parameters = parameters.map { tryConvertToTarget(mappings, it) },
            returnType = tryConvertToTarget(mappings, returnType.substringAfterLast(':'))
        ).getBytecodeDescriptor()

    private fun renameField(field: FieldMapping, inputNamespace: String, targetNamespace: String, clazz: ClassDef): FieldMapping {
        val def = clazz.fields.find {
            it.getName(inputNamespace) == field.to && (inputNamespace == "intermediary" || it.getDescriptor(inputNamespace) == field.getDescriptor())
        } ?: return field

        return field.copy(to = def.getName(targetNamespace))
    }

    private fun renameMethod(method: MethodMapping, inputNamespace: String, targetNamespace: String, clazz: ClassDef): MethodMapping {
        val def = clazz.methods.find {
            it.getName(inputNamespace) == method.to && (inputNamespace == "intermediary" || it.getDescriptor(inputNamespace) == method.getDescriptor())
        } ?: return method

        return method.copy(to = def.getName(targetNamespace))
    }
}
