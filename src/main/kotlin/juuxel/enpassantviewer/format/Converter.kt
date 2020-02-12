package juuxel.enpassantviewer.format

import io.github.cottonmc.proguardparser.ProjectMapping

interface Converter<in T> {
    fun toProguard(mappings: T): ProjectMapping
}
