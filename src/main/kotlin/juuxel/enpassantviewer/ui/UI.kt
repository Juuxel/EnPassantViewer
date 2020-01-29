package juuxel.enpassantviewer.ui

import java.awt.BorderLayout
import javax.swing.*

class UI : JPanel() {
    val tree = JTree(arrayOf("Hello"))
    var treeView: TreeView = TreeView.Separate

    init {
        layout = BorderLayout()

        add(JScrollPane(tree), BorderLayout.CENTER)
    }

    enum class TreeView {
        Separate,
        Tree;

        val createPackageTree: Boolean
            get() = this == Tree
    }
}
