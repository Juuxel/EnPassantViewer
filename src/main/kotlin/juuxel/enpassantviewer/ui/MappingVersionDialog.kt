package juuxel.enpassantviewer.ui

import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

class MappingVersionDialog(parent: JFrame) : JDialog(parent) {
    private val cb: JComboBox<String>
    private var isCancelled = false

    init {
        cb = JComboBox(arrayOf("Latest release", "Latest snapshot", "Custom version..."))
        cb.isEditable = true

        val okButton = JButton(action("Ok") { isVisible = false })
        val cancelButton = JButton(action("Cancel") { isVisible = false; isCancelled = true })

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        buttonPanel.add(Box.createHorizontalGlue())
        buttonPanel.add(cancelButton)
        buttonPanel.add(Box.createHorizontalStrut(5))
        buttonPanel.add(okButton)

        val pane = JPanel()
        pane.layout = BorderLayout()
        pane.add(cb, BorderLayout.CENTER)
        pane.add(buttonPanel, BorderLayout.SOUTH)

        title = "Select version"
        contentPane = pane
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        isModal = true
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                isCancelled = true
                isVisible = false
            }
        })

        pack()
    }

    fun requestInput(): Result {
        isVisible = true
        return when {
            isCancelled -> Result.Cancelled
            else -> when (val version = cb.selectedItem?.toString() ?: "null") {
                "Latest release" -> Result.LatestRelease
                "Latest snapshot" -> Result.LatestSnapshot
                else -> Result.Custom(version)
            }
        }
    }

    sealed class Result {
        object LatestRelease : Result() {
            override fun toString() = "Latest release"
        }

        object LatestSnapshot : Result() {
            override fun toString() = "Latest snapshot"
        }

        object Cancelled : Result()

        data class Custom(var version: String) : Result()
    }
}
