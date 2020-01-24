package juuxel.enpassantviewer.ui

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*

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
    }

    fun requestInput(): Map<String, String> {
        isVisible = true
        pack()
        return fields.mapValues { (_, field) -> field.text }
    }
}
