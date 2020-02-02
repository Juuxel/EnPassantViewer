package juuxel.enpassantviewer.ui

import java.awt.Component
import java.util.logging.Level
import org.jdesktop.swingx.JXErrorPane
import org.jdesktop.swingx.error.ErrorInfo

object ErrorReporter {
    fun run(parent: Component, message: String, fn: () -> Unit) {
        try {
            fn()
        } catch (e: Exception) {
            JXErrorPane.showDialog(parent, ErrorInfo(message, message, null, null, e, Level.SEVERE, null))
        }
    }
}
