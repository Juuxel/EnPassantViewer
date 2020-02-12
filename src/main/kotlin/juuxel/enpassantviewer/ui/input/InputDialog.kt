package juuxel.enpassantviewer.ui.input

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class InputDialog(prompt: String, properties: Map<String, String>) : JDialog() {
    private val fields: Map<String, JTextField>

    init {
        isModal = true
        contentPane = JPanel(BorderLayout())
        contentPane.add(JLabel(prompt), BorderLayout.NORTH)

        val mainPanel = JPanel().apply {
            layout = GridLayout(0, 2)

            fields = properties.mapValues { (_, default) -> JTextField(default) }
            for ((name, field) in fields) {
                add(JLabel(name))
                add(field)
            }
        }
        val buttonPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.RIGHT)
            val ok = JButton("Ok")
            ok.addActionListener {
                this@InputDialog.dispose()
            }
            add(ok)
        }

        contentPane.add(mainPanel, BorderLayout.CENTER)
        contentPane.add(buttonPanel, BorderLayout.SOUTH)

        pack()
    }

    fun requestInput(): Map<String, String> {
        isVisible = true
        return fields.mapValues { (_, field) -> field.text }
    }

    companion object {
        fun requestTinyNamespaces(defaultInputNamespace: String, defaultTargetNamespace: String): Pair<String, String> {
            val input = InputDialog(
                "<html><h1>Select namespaces",
                mapOf(
                    "Input namespace" to defaultInputNamespace,
                    "Target namespace" to defaultTargetNamespace
                )
            )
            val result = input.requestInput()
            val inputNamespace = result["Input namespace"]!!
            val targetNamespace = result["Target namespace"]!!

            return inputNamespace to targetNamespace
        }
    }
}
