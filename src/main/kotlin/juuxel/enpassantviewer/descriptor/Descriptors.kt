package juuxel.enpassantviewer.descriptor

import juuxel.leafthrough.StringReader

object Descriptors {
    private val PRIMITIVES = charArrayOf('I', 'S', 'J', 'B', 'C', 'D', 'F', 'Z', 'V')

    fun readableToDescriptor(type: String): String =
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
                type.endsWith("[]") -> '[' + readableToDescriptor(type.substringBeforeLast('['))
                else -> "L${type.replace('.', '/')};"
            }
        }

    fun descriptorToReadable(type: String): String =
        when (type) {
            "I" -> "int"
            "S" -> "short"
            "J" -> "long"
            "B" -> "byte"
            "C" -> "char"
            "D" -> "double"
            "F" -> "float"
            "Z" -> "boolean"
            "V" -> "void"
            else -> when {
                type.startsWith('[') -> descriptorToReadable(type.substring(1)) + "[]"
                type.startsWith('L') && type.endsWith(';') -> type.substring(1, type.length - 1).replace('/', '.')
                else -> throw IllegalArgumentException("Unknown type descriptor format: $type")
            }
        }

    fun isPrimitive(c: Char) = c in PRIMITIVES
}

/**
 * A method descriptor.
 *
 * @property parameters the method parameters in Proguard format
 * @property returnType the method return type in Proguard format
 */
data class MethodDescriptor(val parameters: List<String>, val returnType: String) {
    fun getBytecodeDescriptor(): String =
        '(' + parameters.joinToString(separator = "") { Descriptors.readableToDescriptor(it) } +
                ')' + Descriptors.readableToDescriptor(returnType)

    companion object {
        fun fromDescriptor(descriptor: String): MethodDescriptor {
            val parameters = ArrayList<String>()
            val returnType: String

            val descriptorReader = StringReader(descriptor)
            val buffer = ArrayList<Char>()
            var insideLiteral = false

            descriptorReader.expect('(')
            while (descriptorReader.peek() != ')') {
                val current = descriptorReader.next()
                buffer.add(current)

                val isPrimitive = !insideLiteral && Descriptors.isPrimitive(current)
                if (isPrimitive || current == ';') {
                    parameters.add(Descriptors.descriptorToReadable(buffer.joinToString(separator = "")))
                    buffer.clear()
                    insideLiteral = false
                } else if (current == 'L') {
                    insideLiteral = true
                }
            }
            descriptorReader.expect(')')
            returnType = descriptorReader.getRemaining()

            return MethodDescriptor(parameters, Descriptors.descriptorToReadable(returnType))
        }
    }
}
