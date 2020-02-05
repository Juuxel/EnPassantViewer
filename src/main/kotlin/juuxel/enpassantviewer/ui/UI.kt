package juuxel.enpassantviewer.ui

import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import juuxel.enpassantviewer.ui.status.EPStatusBar
import juuxel.enpassantviewer.ui.status.StatusManager

class UI : JPanel() {
    val tree = JTree(arrayOf("Hello"))
    var treeView: TreeView = TreeView.Separate
    val statusManager: StatusManager

    init {
        layout = BorderLayout()
        val statusBar = EPStatusBar()
        statusManager = statusBar

        add(JScrollPane(tree), BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)
    }

    enum class TreeView {
        Separate,
        Tree;

        val createPackageTree: Boolean
            get() = this == Tree
    }
}
