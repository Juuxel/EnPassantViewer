package juuxel.enpassantviewer.transformation

import io.github.cottonmc.proguardparser.*
import juuxel.enpassantviewer.ui.InputDialog
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.TinyMappingFactory
import java.io.File

class ComposeWithTiny(private val mappings: ProjectMapping) {
    fun run(tinyFile: File): ProjectMapping {
        val tree = tinyFile.bufferedReader().use { reader -> TinyMappingFactory.loadWithDetection(reader) }
        val namespaces = tree.metadata.namespaces
        val defaultTargetNamespace = if ("named" in namespaces) "named" else "intermediary"
        val defaultInputNamespace = if (defaultTargetNamespace == "named") "intermediary" else "official"

        val input = InputDialog(
            "Select namespaces:",
            mapOf(
                "Input namespace" to defaultInputNamespace,
                "Target namespace" to defaultTargetNamespace
            )
        )
        val result = input.requestInput()
        val inputNamespace = result["Input namespace"]!!
        val targetNamespace = result["Target namespace"]!!

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

    private fun typeToDescriptor(type: String): String =
        when (type) {
            "int" -> "I"
            "short" -> "S"
            "long" -> "J"
            "byte" -> "B"
            "char" -> "C"
            "double" -> "D"
            "float" -> "F"
            "boolean" -> "Z"
            "void" -> "V"
            else -> when {
                type.endsWith("[]") -> "[${typeToDescriptor(type.substringBeforeLast('['))}"
                else -> "L${(mappings.classes.find { it.from == type }?.to ?: type).replace('.', '/')};"
            }
        }

    private fun FieldMapping.getDescriptor() = typeToDescriptor(type)

    private fun MethodMapping.getDescriptor(): String {
        // TODO: Fix param parsing
        val params = if (parameters.size == 1 && parameters[0] == "") "" else parameters.joinToString(separator = "", transform = ::typeToDescriptor)
        val type = typeToDescriptor(returnType.substringAfterLast(':'))

        return "($params)$type"
    }

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
