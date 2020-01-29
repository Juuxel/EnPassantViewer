package juuxel.enpassantviewer

import com.alee.laf.WebLookAndFeel
import juuxel.enpassantviewer.ui.ViewerWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

suspend fun main() = withContext(Dispatchers.Swing) {
    WebLookAndFeel.install()
    val window = ViewerWindow()
    window.isVisible = true
}
