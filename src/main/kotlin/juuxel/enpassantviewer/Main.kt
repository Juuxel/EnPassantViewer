package juuxel.enpassantviewer

import com.alee.laf.WebLookAndFeel
import juuxel.enpassantviewer.ui.ViewerWindow
import javax.swing.SwingUtilities

fun main() {
    //UIManager.setLookAndFeel(NimbusLookAndFeel())

    SwingUtilities.invokeLater {
        WebLookAndFeel.install()
        val window = ViewerWindow()
        window.isVisible = true
    }
}
