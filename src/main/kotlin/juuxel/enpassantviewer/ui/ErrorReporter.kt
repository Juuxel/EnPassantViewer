package juuxel.enpassantviewer.ui

import org.jdesktop.swingx.JXErrorPane
import org.jdesktop.swingx.error.ErrorInfo
import java.awt.Component
import java.util.logging.Level

object ErrorReporter {
    fun run(parent: Component, message: String, fn: () -> Unit) {
        try {
            fn()
        } catch (e: Exception) {
            JXErrorPane.showDialog(parent, ErrorInfo(message, message, null, null, e, Level.SEVERE, null))
        }
    }
}
