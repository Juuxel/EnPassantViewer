package juuxel.enpassantviewer.action.mappings

import io.github.cottonmc.proguardparser.ProjectMapping

fun tryConvertToTarget(mappings: ProjectMapping, type: String): String {
    val nonArrayPart = type.substringBefore('[')
    val arrayPart = if ('[' in type) "[" + type.substringAfter('[') else ""
    return mappings.findClassOrNull(nonArrayPart)?.to?.let { name -> "$name$arrayPart" } ?: type
}
