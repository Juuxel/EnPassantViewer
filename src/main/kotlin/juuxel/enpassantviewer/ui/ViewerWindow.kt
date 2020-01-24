package juuxel.enpassantviewer.ui

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import juuxel.enpassantviewer.transformation.ComposeWithTiny
import juuxel.enpassantviewer.transformation.Invert
import org.jdesktop.swingx.JXErrorPane
import org.jdesktop.swingx.error.ErrorInfo
import java.awt.Dimension
import java.io.File
import java.util.logging.Level
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.tree.DefaultTreeModel

class ViewerWindow : JFrame() {
    private val ui = UI()
    private lateinit var currentMappings: ProjectMapping

    init {
        contentPane = ui
        title = "En Passant Viewer"
        size = Dimension(640, 480)
        defaultCloseOperation = EXIT_ON_CLOSE
        iconImage = ImageIO.read(ViewerWindow::class.java.getResourceAsStream("/icon.png"))

        val menu = JMenuBar()
        val fileMenu = JMenu("File")
        val openButton = JMenuItem("Open")
        openButton.addActionListener {
            val chooser = JFileChooser()
            chooser.isMultiSelectionEnabled = false
            val result = chooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                loadMappings(chooser.selectedFile)
            }
        }
        fileMenu.add(openButton)

        val transformMenu = JMenu("Transform")
        val composeTinyButton = JMenuItem("Compose with Tiny")
        composeTinyButton.addActionListener {
            val chooser = JFileChooser()
            chooser.isMultiSelectionEnabled = false
            val result = chooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    val newMappings = ComposeWithTiny(currentMappings).run(chooser.selectedFile)
                    setMappings(newMappings)
                } catch (e: Throwable) {
                    JXErrorPane.showDialog(this, ErrorInfo("Error while composing with tiny", null, null, null, e, Level.SEVERE, null))
                }
            }
        }
        val invertButton = JMenuItem("Invert")
        invertButton.addActionListener {
            try {
                setMappings(Invert.run(currentMappings))
            } catch (e: Throwable) {
                // TODO: Extract this
                JXErrorPane.showDialog(this, ErrorInfo("Error while composing with tiny", null, null, null, e, Level.SEVERE, null))
            }
        }

        transformMenu.add(composeTinyButton)
        transformMenu.add(invertButton)

        menu.add(fileMenu)
        menu.add(transformMenu)
        jMenuBar = menu
    }

    private fun loadMappings(file: File) {
        try {
            val mappings = parseProguardMappings(file.readLines())
            setMappings(mappings)
        } catch (e: Exception) {
            JXErrorPane.showDialog(this, ErrorInfo("Error while parsing mappings", null, null, null, e, Level.SEVERE, null))
        }
    }

    private fun setMappings(mappings: ProjectMapping) {
        currentMappings = mappings
        ui.tree.model = DefaultTreeModel(MappingsTreeNode.Root(mappings))
    }
}