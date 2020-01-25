package juuxel.enpassantviewer.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import javax.swing.*

interface StepManager {
    var step: String
}

class ProgressDialog(parent: JFrame, message: String) : JDialog(parent), StepManager {
    private val label = JLabel(message)

    override var step = message
        set(value) {
            field = value
            runBlocking(Dispatchers.Swing) {
                label.text = value
            }
        }

    init {
        title = message
        isModal = true

        contentPane = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(label)
            add(JProgressBar().apply { isIndeterminate = true })
        }

        pack()
    }

    companion object {
        fun show(parent: JFrame, message: String, fn: StepManager.() -> Unit) {
            val dialog = ProgressDialog(parent, message)
            GlobalScope.launch {
                ErrorReporter.run(parent, "Error: $message") { fn(dialog) }
                dialog.isVisible = false
            }
            dialog.isVisible = true
        }
    }
}
