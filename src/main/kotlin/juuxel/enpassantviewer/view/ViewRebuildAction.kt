package juuxel.enpassantviewer.view

import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.ui.MappingsTreeNode
import juuxel.enpassantviewer.ui.UI
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class ViewRebuildAction(
    private val mappings: () -> ProjectMapping,
    private val setter: (MappingsTreeNode, UI.TreeView) -> Unit,
    private val viewMode: UI.TreeView
) : AbstractAction(viewMode.toString()) {
    override fun actionPerformed(e: ActionEvent?) =
        setter(MappingsTreeNode.Root(mappings(), viewMode.createPackageTree), viewMode)
}
