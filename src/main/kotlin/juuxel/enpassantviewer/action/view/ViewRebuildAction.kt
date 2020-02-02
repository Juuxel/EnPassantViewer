package juuxel.enpassantviewer.action.view

import io.github.cottonmc.proguardparser.ProjectMapping
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import juuxel.enpassantviewer.ui.MappingsTreeNode
import juuxel.enpassantviewer.ui.UI

class ViewRebuildAction(
    private val mappings: () -> ProjectMapping,
    private val setter: (MappingsTreeNode, UI.TreeView) -> Unit,
    private val viewMode: UI.TreeView
) : AbstractAction(viewMode.toString()) {
    override fun actionPerformed(e: ActionEvent?) =
        setter(MappingsTreeNode.Root(mappings(), viewMode.createPackageTree), viewMode)
}
