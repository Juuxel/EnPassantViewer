package juuxel.enpassantviewer.ui

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import io.github.cottonmc.proguardparser.toProguardMappings
import juuxel.enpassantviewer.analysis.FindLostClasses
import juuxel.enpassantviewer.analysis.FindUnobfuscatedClasses
import juuxel.enpassantviewer.mappings.OpenMojmap
import juuxel.enpassantviewer.transformation.ComposeWithIntermediary
import juuxel.enpassantviewer.transformation.ComposeWithYarn
import juuxel.enpassantviewer.transformation.ComposeWithTiny
import juuxel.enpassantviewer.transformation.Invert
import juuxel.enpassantviewer.view.ViewRebuildAction
import java.awt.Dimension
import java.awt.Event
import java.awt.event.KeyEvent
import java.io.File
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

        val save = action("Save") {
            val result = fileChooser.showSaveDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                saveMappings(fileChooser.selectedFile)
            }
        }

        val openButton = JMenuItem(open)
        openButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK)
        val saveButton = JMenuItem(save)
        saveButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK)
        fileMenu.add(openButton)
        fileMenu.add(OpenMojmap(this, this::setMappings))
        fileMenu.add(saveButton)

        val viewMenu = JMenu("View")
        val viewModeGroup = ButtonGroup()

        fun createViewRebuildButton(viewMode: UI.TreeView) =
            JRadioButtonMenuItem(
                ViewRebuildAction(
                    mappings = { currentMappings },
                    setter = { tree, mode ->
                        ui.tree.model = DefaultTreeModel(tree)
                        ui.treeView = mode
                    },
                    viewMode = viewMode
                )
            )

        val separateButton = createViewRebuildButton(UI.TreeView.Separate)
        val treeButton = createViewRebuildButton(UI.TreeView.Tree)

        viewModeGroup.add(treeButton)
        viewModeGroup.add(separateButton)
        separateButton.isSelected = true

        viewMenu.add(separateButton)
        viewMenu.add(treeButton)

        val transformMenu = JMenu("Transform")
        val composeWithTiny = action("Compose with Tiny") {
            val result = fileChooser.showOpenDialog(this)
            if (result == JFileChooser.APPROVE_OPTION) {
                ProgressDialog.show(this, "Composing mappings") {
                    val newMappings = fileChooser.selectedFile.bufferedReader().use {
                        ComposeWithTiny(currentMappings).run(it)
                    }
                    setMappings(newMappings)
                }
            }
        }
        val invert = action("Invert") {
            ErrorReporter.run(this, "Error while inverting mappings") {
                setMappings(Invert.run(currentMappings))
            }
        }

        val composeTinyButton = JMenuItem(composeWithTiny)
        composeTinyButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK)
        val invertButton = JMenuItem(invert)
        invertButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK)

        transformMenu.add(composeTinyButton)
        transformMenu.add(ComposeWithIntermediary(this, { currentMappings }, this::setMappings))
        transformMenu.add(ComposeWithYarn(this, { currentMappings }, this::setMappings))
        transformMenu.add(invertButton)

        val analysisMenu = JMenu("Analysis")
        analysisMenu.add(FindLostClasses { currentMappings })
        analysisMenu.add(FindUnobfuscatedClasses { currentMappings })

        menu.add(fileMenu)
        menu.add(viewMenu)
        menu.add(transformMenu)
        menu.add(analysisMenu)
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
        ProgressDialog.show(this, "Parsing mappings") {
            val mappings = parseProguardMappings(file.readLines())
            setMappings(mappings)
        }
    }

    private fun saveMappings(file: File) {
        ProgressDialog.show(this, "Saving mappings") {
            file.writeText(currentMappings.toProguardMappings().joinToString(separator = "", transform = { "$it\n" }))
        }
    }

    private fun setMappings(mappings: ProjectMapping) {
        currentMappings = mappings
        ui.tree.model = DefaultTreeModel(MappingsTreeNode.Root(mappings, ui.treeView.createPackageTree))
    }
}