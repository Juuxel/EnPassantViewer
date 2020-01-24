package juuxel.enpassantviewer.ui

import io.github.cottonmc.proguardparser.*
import java.util.*
import javax.swing.tree.TreeNode

sealed class MappingsTreeNode : TreeNode {
    class Root(mappings: ProjectMapping) : MappingsTreeNode() {
        private val packages =
            mappings.classes.map { it.from.substringBeforeLast('.', "<root package>") }
                .distinct().sorted()
                .map { Package(this, it, mappings.classes.filter { c -> c.from.substringBeforeLast('.', "<root package>") == it }) }
                .let { Vector(it) }

        override fun getParent() = null
        override fun children(): Enumeration<*> = packages.elements()
        override fun getChildCount() = packages.size
        override fun getChildAt(childIndex: Int): TreeNode? = packages[childIndex]
        override fun getIndex(node: TreeNode) = packages.indexOf(node)
        override fun getAllowsChildren() = true
        override fun isLeaf() = false
    }

    class Package(private val parent: MappingsTreeNode?, val name: String, classes: List<ClassMapping>) :
        MappingsTreeNode() {
        private val children =
            classes.sortedBy { it.from }
                .map { Type(this, it) }
                .let { Vector(it) }

        override fun getParent() = parent
        override fun children(): Enumeration<*> = children.elements()
        override fun getChildCount() = children.size
        override fun getChildAt(childIndex: Int): TreeNode? = children[childIndex]
        override fun getIndex(node: TreeNode) = children.indexOf(node)
        override fun getAllowsChildren() = true
        override fun isLeaf() = false

        override fun toString() = name
    }

    class Type(private val parent: MappingsTreeNode?, val clazz: ClassMapping) : MappingsTreeNode() {
        private val methods = clazz.methods.sortedBy { it.from }.map { Method(this, it) }
        private val fields = clazz.fields.sortedBy { it.from }.map { Field(this, it) }
        private val children = Vector(fields + methods)

        override fun getParent() = parent
        override fun children(): Enumeration<*> = children.elements()
        override fun isLeaf() = childCount == 0
        override fun getChildCount() = children.size
        override fun getChildAt(childIndex: Int): TreeNode? = children[childIndex]
        override fun getIndex(node: TreeNode) = children.indexOf(node)
        override fun getAllowsChildren() = true

        override fun toString() = "${clazz.fromSimpleName} → ${clazz.to}"
    }

    class Method(private val parent: MappingsTreeNode, val method: MethodMapping) : MappingsTreeNode() {
        override fun getParent() = parent
        override fun children(): Enumeration<*> = EmptyEnumeration
        override fun isLeaf() = true
        override fun getChildCount() = 0
        override fun getChildAt(childIndex: Int) = null
        override fun getIndex(node: TreeNode?) = -1
        override fun getAllowsChildren() = false

        override fun toString() = "${method.returnType} ${method.from}(${method.parameters.joinToString()}) → ${method.to}"
    }

    class Field(private val parent: MappingsTreeNode, val field: FieldMapping) : MappingsTreeNode() {
        override fun getParent() = parent
        override fun children(): Enumeration<*> = EmptyEnumeration
        override fun isLeaf() = true
        override fun getChildCount() = 0
        override fun getChildAt(childIndex: Int) = null
        override fun getIndex(node: TreeNode?) = -1
        override fun getAllowsChildren() = false

        override fun toString() = "${field.type} ${field.from} → ${field.to}"
    }

    private object EmptyEnumeration : Enumeration<Nothing> {
        override fun hasMoreElements() = false
        override fun nextElement() = throw NoSuchElementException()
    }
}
