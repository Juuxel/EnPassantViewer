package juuxel.enpassantviewer

import juuxel.enpassantviewer.ui.ViewerWindow
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.nimbus.NimbusLookAndFeel

fun main() {
    UIManager.setLookAndFeel(NimbusLookAndFeel())

    val window = ViewerWindow()
    SwingUtilities.invokeLater {
        window.isVisible = true
    }
}
