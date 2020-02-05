package juuxel.enpassantviewer.ui.progress

import java.awt.GridLayout
import java.util.ArrayDeque
import java.util.Deque
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import juuxel.enpassantviewer.ui.ErrorReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing

class ProgressDialog(parent: JFrame, message: String) : JDialog(parent), StepManager {
    private val label = JLabel(message)

    private val stepStack: Deque<String> = ArrayDeque()

    override var step: String
        get() = stepStack.pollLast() ?: "Unknown"
        set(value) {
            if (stepStack.size > 1) popStep()
            pushStep(value)
        }

    init {
        title = message
        isModal = true

        contentPane = JPanel().apply {
            layout = GridLayout(0, 1)

            add(label)
            add(JProgressBar().apply { isIndeterminate = true })
        }

        stepStack.push(message)

        pack()
    }

    override fun pushStep(step: String) {
        stepStack.push(step)
        runBlocking(Dispatchers.Swing) {
            label.text = step
            pack()
        }
    }

    override fun popStep() {
        stepStack.pop()
        runBlocking(Dispatchers.Swing) {
            label.text = step
            pack()
        }
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
