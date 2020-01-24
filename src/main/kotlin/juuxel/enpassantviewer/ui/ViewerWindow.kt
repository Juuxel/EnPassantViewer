package juuxel.enpassantviewer.ui

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import juuxel.enpassantviewer.transformation.ComposeWithTiny
import juuxel.enpassantviewer.transformation.Invert
import org.jdesktop.swingx.JXErrorPane
import org.jdesktop.swingx.error.ErrorInfo
import java.awt.Dimension
import java.awt.Event
import java.awt.event.KeyEvent
import java.io.File
import java.util.logging.Level
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.tree.DefaultTreeModel

class ViewerWindow : JFrame() {
    private val ui = UI()
    private val fileChooser = JFileChooser()
    private lateinit var currentMappings: ProjectMapping

    init {
        contentPane = ui
        title = "En Passant Viewer"
        size = Dimension(640, 480)
        defaultCloseOperation = EXIT_ON_CLOSE
        iconImage = ImageIO.read(ViewerWindow::class.java.getResourceAsStream("/icon.png"))

        val menu = JMenuBar()
        val fileMenu = JMenu("File")
        val open = action("Open") {
            val result = fileChooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                loadMappings(fileChooser.selectedFile)
            }
        }

        val openButton = JMenuItem(open)
        openButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK)
        fileMenu.add(openButton)

        val transformMenu = JMenu("Transform")
        val composeWithTiny = action("Compose with Tiny") {
            val result = fileChooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    ProgressDialog.show(this, "Composing mappings") {
                        val newMappings = ComposeWithTiny(currentMappings).run(fileChooser.selectedFile)
                        setMappings(newMappings)
                    }
                } catch (e: Throwable) {
                    JXErrorPane.showDialog(this, ErrorInfo("Error while composing with tiny", null, null, null, e, Level.SEVERE, null))
                }
            }
        }
        val invert = action("Invert") {
            try {
                setMappings(Invert.run(currentMappings))
            } catch (e: Throwable) {
                // TODO: Extract this
                JXErrorPane.showDialog(this, ErrorInfo("Error while composing with tiny", null, null, null, e, Level.SEVERE, null))
            }
        }

        val composeTinyButton = JMenuItem(composeWithTiny)
        composeTinyButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK)
        val invertButton = JMenuItem(invert)
        invertButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK)

        transformMenu.add(composeTinyButton)
        transformMenu.add(invertButton)

        menu.add(fileMenu)
        menu.add(transformMenu)
        jMenuBar = menu

        /*ui.inputMap.let { input ->
            input.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK), "open")
            input.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), "invert")
            input.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "promote")
        }

        ui.actionMap.let { actions ->
            actions.put("open", open)
            actions.put("invert", invert)
            actions.put("promote", composeWithTiny)
        }*/

        ui.requestFocusInWindow()
    }

    private fun loadMappings(file: File) {
        try {
            ProgressDialog.show(this, "Parsing mappings") {
                val mappings = parseProguardMappings(file.readLines())
                setMappings(mappings)
            }
        } catch (e: Exception) {
            JXErrorPane.showDialog(this, ErrorInfo("Error while parsing mappings", null, null, null, e, Level.SEVERE, null))
        }
    }

    private fun setMappings(mappings: ProjectMapping) {
        currentMappings = mappings
        ui.tree.model = DefaultTreeModel(MappingsTreeNode.Root(mappings))
    }
}