package juuxel.enpassantviewer.ui

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.*

class ProgressDialog(parent: JFrame, message: String) : JDialog(parent) {
    init {
        title = message
        isModal = true

        contentPane = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel(message))
            add(JProgressBar().apply { isIndeterminate = true })
        }

        pack()
    }

    companion object {
        fun show(parent: JFrame, message: String, fn: () -> Unit) {
            val dialog = ProgressDialog(parent, message)
            GlobalScope.launch {
                fn()
                dialog.isVisible = false
            }
            dialog.isVisible = true
        }
    }
}
