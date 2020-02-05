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
            report(parent, message, e)
        }
    }

    fun report(parent: Component, message: String?, error: Exception) {
        val msg: String = message ?: "${error::class.java.name}: ${error.message}"
        JXErrorPane.showDialog(parent, ErrorInfo(msg, msg, null, null, error, Level.SEVERE, null))
    }
}
