package juuxel.enpassantviewer.view

import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.ui.MappingsTreeNode
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class RebuildAsTree(
    private val mappings: () -> ProjectMapping,
    private val setter: (MappingsTreeNode) -> Unit
) : AbstractAction("Rebuild as Tree") {
    override fun actionPerformed(e: ActionEvent?) =
        setter(MappingsTreeNode.Root(mappings(), createPackageTree = true))
}

class RebuildAsSeparate(
    private val mappings: () -> ProjectMapping,
    private val setter: (MappingsTreeNode) -> Unit
) : AbstractAction("Rebuild as Separate") {
    override fun actionPerformed(e: ActionEvent?) =
        setter(MappingsTreeNode.Root(mappings(), createPackageTree = false))
}
