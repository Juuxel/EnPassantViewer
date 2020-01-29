package juuxel.enpassantviewer.action.analysis

import io.github.cottonmc.proguardparser.*
import juuxel.enpassantviewer.ui.MappingsTreeNode
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.tree.DefaultTreeModel

class FindUnobfuscatedClasses(
    private val mappings: () -> ProjectMapping
) : AbstractAction("Find Unobfuscated Classes") {
    override fun actionPerformed(e: ActionEvent?) {
        val m = mappings()
        val newMappings = ProjectMapping.classes.modify(m) {
            it.filter { c -> c.from == c.to }
        }
        val dialog = ResultDialog(MappingsTreeNode.Root(newMappings))
        dialog.isVisible = true
    }

    private class ResultDialog(tree: MappingsTreeNode) : JDialog() {
        init {
            title = "Unobfuscated Classes"
            contentPane = JScrollPane(JTree(DefaultTreeModel(tree)))
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            pack()
        }
    }
}
