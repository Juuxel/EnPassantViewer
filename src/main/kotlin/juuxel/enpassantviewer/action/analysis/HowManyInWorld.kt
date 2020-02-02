package juuxel.enpassantviewer.action.analysis

import io.github.cottonmc.proguardparser.ProjectMapping
import java.awt.event.ActionEvent
import java.text.NumberFormat
import javax.swing.AbstractAction
import javax.swing.JFrame
import javax.swing.JOptionPane

class HowManyInWorld(
    private val frame: JFrame,
    private val mappings: () -> ProjectMapping
) : AbstractAction("How Many in World?") {
    override fun actionPerformed(e: ActionEvent?) {
        val classes = mappings().classes.filter { it.fromSimpleName != "package-info" && it.toSimpleName != "package-info" }
        val worldClasses = classes.count { it.from.startsWith("net.minecraft.world.") }
        val packages = mappings().classes.asSequence()
            .map { it.from.substringBeforeLast('.', "") }
            .filter { it.isNotEmpty() }
            .distinct()
        val packageCount = packages.count()
        val worldPackages = packages.count { it.startsWith("net.minecraft.world") }

        val classFraction = worldClasses.toDouble() / classes.size.toDouble()
        val packageFraction = worldPackages.toDouble() / packageCount.toDouble()

        val message =
            """
                <html><b>In the world package:</b><br>
                ${NumberFormat.getPercentInstance().format(classFraction)} of classes
                ${NumberFormat.getPercentInstance().format(packageFraction)} of packages
                $worldClasses out of ${classes.size} classes
                $worldPackages out of $packageCount packages
            """.trimIndent()

        JOptionPane.showMessageDialog(frame, message, "How Many in World?", JOptionPane.INFORMATION_MESSAGE)
    }
}
