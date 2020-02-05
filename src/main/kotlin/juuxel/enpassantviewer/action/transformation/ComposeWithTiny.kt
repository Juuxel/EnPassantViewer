package juuxel.enpassantviewer.action.transformation

import io.github.cottonmc.proguardparser.FieldMapping
import io.github.cottonmc.proguardparser.MethodMapping
import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.classes
import java.io.BufferedReader
import juuxel.enpassantviewer.ui.input.InputDialog
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.TinyMappingFactory

class ComposeWithTiny(private val mappings: ProjectMapping) {
    fun run(tinyReader: BufferedReader): ProjectMapping {
        val tree = TinyMappingFactory.loadWithDetection(tinyReader)
        val namespaces = tree.metadata.namespaces
        val defaultInputNamespace = namespaces.first()
        val defaultTargetNamespace = namespaces.last()

        val input = InputDialog(
            "<html><h1>Select namespaces",
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
                else -> "L${(mappings.findClassOrNull(type)?.to ?: type).replace('.', '/')};"
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
