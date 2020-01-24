package juuxel.enpassantviewer.ui

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

fun action(name: String, fn: () -> Unit) = object : AbstractAction(name) {
    override fun actionPerformed(e: ActionEvent?) = fn()
}
