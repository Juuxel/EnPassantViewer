package juuxel.enpassantviewer.action.mappings

import io.github.cottonmc.proguardparser.ProjectMapping

fun tryConvertToTarget(mappings: ProjectMapping, type: String): String {
    val colonPart = if (':' in type) type.substringBeforeLast(':') + ':' else ""
    val nonArrayPart = type.substringAfterLast(':').substringBefore('[')
    val arrayPart = if ('[' in type) "[" + type.substringAfter('[') else ""
    return mappings.findClassOrNull(nonArrayPart)?.to?.let { name ->
        "$colonPart$name$arrayPart"
    } ?: type
}
