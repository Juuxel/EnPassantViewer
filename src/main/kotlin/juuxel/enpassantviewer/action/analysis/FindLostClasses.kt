package juuxel.enpassantviewer.action.analysis

import io.github.cottonmc.proguardparser.ClassMapping
import io.github.cottonmc.proguardparser.ProjectMapping
import juuxel.enpassantviewer.ui.ErrorReporter
import juuxel.enpassantviewer.ui.action
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.text.MessageFormat
import javax.swing.*
import javax.swing.table.AbstractTableModel

class FindLostClasses(
    private val mappings: () -> ProjectMapping
) : AbstractAction("Find Lost Classes") {
    override fun actionPerformed(e: ActionEvent?) {
        val m = mappings()
        val lostClasses = m.classes.filter {
            it.fromSimpleName != "package-info" && '.' !in it.to
        }
        val dialog = ResultDialog(lostClasses)
        dialog.isVisible = true
    }

    private class ResultDialog(classes: List<ClassMapping>) : JDialog() {
        init {
            title = "Lost Classes"
            contentPane = JPanel().apply {
                layout = BorderLayout()
                val table = JTable(ResultTableModel(classes))
                table.columnModel.getColumn(0).preferredWidth = 540
                val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
                val printButton = JButton(action("Print") {
                    ErrorReporter.run(this@ResultDialog, "Printing failed") {
                        table.print(
                            JTable.PrintMode.FIT_WIDTH,
                            MessageFormat("Lost Classes"),
                            null
                        )
                    }
                })
                buttonPanel.add(printButton)

                add(JScrollPane(table), BorderLayout.CENTER)
                add(buttonPanel, BorderLayout.SOUTH)
            }
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            setSize(640, 480)
        }
    }

    private class ResultTableModel(private val classes: List<ClassMapping>) : AbstractTableModel() {
        override fun getColumnName(column: Int) =
            when (column) {
                0 -> "Unobfuscated"
                1 -> "Obfuscated"
                else -> super.getColumnName(column + 2)
            }

        override fun getRowCount() = classes.size
        override fun getColumnCount() = 2

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val clazz = classes[rowIndex]
            return if (columnIndex == 0) clazz.from else clazz.to
        }
    }
}
