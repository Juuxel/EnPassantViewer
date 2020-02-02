package juuxel.enpassantviewer.ui

import io.github.cottonmc.proguardparser.ClassMapping
import io.github.cottonmc.proguardparser.FieldMapping
import io.github.cottonmc.proguardparser.MethodMapping
import io.github.cottonmc.proguardparser.ProjectMapping
import java.util.Enumeration
import java.util.Vector
import javax.swing.tree.TreeNode

sealed class MappingsTreeNode : TreeNode {
    class Root(val mappings: ProjectMapping, createPackageTree: Boolean = false) : MappingsTreeNode() {
        private val packages =
            if (createPackageTree) createPackageTree()
            else mappings.classes.map { it.from.substringBeforeLast('.', "<root package>") }
                .distinct().sorted()
                .map {
                    Package(
                        this, it,
                        mappings.classes.filter { c -> c.from.substringBeforeLast('.', "<root package>") == it }
                    )
                }
                .let { Vector(it) }

        override fun getParent() = null
        override fun children(): Enumeration<Package> = packages.elements()
        override fun getChildCount() = packages.size
        override fun getChildAt(childIndex: Int): TreeNode? = packages[childIndex]
        override fun getIndex(node: TreeNode) = packages.indexOf(node)
        override fun getAllowsChildren() = true
        override fun isLeaf() = false

        private fun createPackageTree(): Vector<Package> {
            val topLevelTrees = HashMap<String, PackageTree>()

            for (c in mappings.classes) {
                if ('.' !in c.from) {
                    topLevelTrees.getOrPut("<root package>") { PackageTree("<root package>") }.classes += c
                    continue
                }

                val parts = c.from.split('.').dropLast(1)
                val targetTree = parts.drop(1)
                    .fold(topLevelTrees.getOrPut(parts[0]) { PackageTree(parts[0]) }) { acc, s ->
                        acc.subpackages.getOrPut(s) { PackageTree(s) }
                    }

                targetTree.classes += c
            }

            return Vector(topLevelTrees.values.map { it.toImmutableTree(this).simplify() })
        }

        private data class PackageTree(
            val name: String,
            val subpackages: MutableMap<String, PackageTree> = HashMap(),
            val classes: MutableList<ClassMapping> = ArrayList()
        ) {
            fun toImmutableTree(parent: MappingsTreeNode): Package =
                Package(parent, name, classes) { self -> subpackages.values.map { it.toImmutableTree(self) } }
        }
    }

    class Package(
        private val parent: MappingsTreeNode?,
        val name: String,
        private val classes: List<ClassMapping>,
        subpackages: (Package) -> List<Package> = { emptyList() }
    ) : MappingsTreeNode() {
        private val packageChildren = subpackages(this).sortedBy { it.name }
        private val classChildren = classes.sortedBy { it.from }.map { Type(this, it) }
        private val children = Vector(packageChildren + classChildren)

        override fun getParent() = parent
        override fun children(): Enumeration<*> = children.elements()
        override fun getChildCount() = children.size
        override fun getChildAt(childIndex: Int): TreeNode? = children[childIndex]
        override fun getIndex(node: TreeNode) = children.indexOf(node)
        override fun getAllowsChildren() = true
        override fun isLeaf() = false

        private fun transferContentsToParent(target: Package): List<Package> =
            packageChildren.map { Package(target, it.name, it.classes, it::transferContentsToParent) }

        fun simplify(): Package =
            if (children.size == 1 && classChildren.isEmpty()) {
                val child = packageChildren.first()
                Package(
                    parent, "$name.${child.name}",
                    child.classes
                ) { child.transferContentsToParent(it).map { p -> p.simplify() } }
            } else this

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

        override fun toString() =
            "${method.returnType} ${method.from}(${method.parameters.joinToString()}) → ${method.to}"
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
