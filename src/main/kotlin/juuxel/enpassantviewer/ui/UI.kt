package juuxel.enpassantviewer.ui

import java.awt.BorderLayout
import javax.swing.*

class UI : JPanel() {
    val tree = JTree(arrayOf("Hello"))
    init {
        layout = BorderLayout()

        add(JScrollPane(tree), BorderLayout.CENTER)
    }
}
