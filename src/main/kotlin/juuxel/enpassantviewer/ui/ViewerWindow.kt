package juuxel.enpassantviewer.ui

import io.github.cottonmc.proguardparser.ProjectMapping
import io.github.cottonmc.proguardparser.parseProguardMappings
import io.github.cottonmc.proguardparser.toProguardMappings
import java.awt.Dimension
import java.awt.Event
import java.awt.event.KeyEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ButtonGroup
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JRadioButtonMenuItem
import javax.swing.KeyStroke
import javax.swing.tree.DefaultTreeModel
import juuxel.enpassantviewer.action.ActionContext
import juuxel.enpassantviewer.action.analysis.FindLostClasses
import juuxel.enpassantviewer.action.analysis.FindUnobfuscatedClasses
import juuxel.enpassantviewer.action.analysis.HowManyInWorld
import juuxel.enpassantviewer.action.mappings.OpenMojmap
import juuxel.enpassantviewer.action.transformation.ComposeWithIntermediary
import juuxel.enpassantviewer.action.transformation.ComposeWithTiny
import juuxel.enpassantviewer.action.transformation.ComposeWithYarn
import juuxel.enpassantviewer.action.transformation.DuplicateOrigin
import juuxel.enpassantviewer.action.transformation.Invert
import juuxel.enpassantviewer.action.view.ViewRebuildAction
import juuxel.enpassantviewer.ui.progress.ProgressDialog
import juuxel.enpassantviewer.ui.status.GameVersion

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

        val gameVersion = { ui.statusManager.currentGameVersion }
        val actionContext = ActionContext(this, { currentMappings }, gameVersion, this::setMappings, this::setAsterisk)

        val openButton = JMenuItem(open)
        openButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK)
        val saveButton = JMenuItem(save)
        saveButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK)
        fileMenu.add(openButton)
        fileMenu.add(OpenMojmap(actionContext))
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
                    setMappings(newMappings, ui.statusManager.currentGameVersion)
                    setAsterisk(true)
                }
            }
        }

        val composeTinyButton = JMenuItem(composeWithTiny)
        composeTinyButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK)
        val invertButton = JMenuItem(Invert(actionContext))
        invertButton.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK)

        transformMenu.add(composeTinyButton)
        transformMenu.add(ComposeWithIntermediary(actionContext))
        transformMenu.add(ComposeWithYarn(actionContext))
        transformMenu.add(invertButton)
        transformMenu.add(DuplicateOrigin(actionContext))

        val analysisMenu = JMenu("Analysis")
        analysisMenu.add(FindLostClasses { currentMappings })
        analysisMenu.add(FindUnobfuscatedClasses { currentMappings })
        analysisMenu.add(HowManyInWorld(this) { currentMappings })

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
            setMappings(mappings, GameVersion.Unknown)
            setAsterisk(false)
        }
    }

    private fun saveMappings(file: File) {
        ProgressDialog.show(this, "Saving mappings") {
            file.writeText(currentMappings.toProguardMappings().joinToString(separator = "", transform = { "$it\n" }))
        }
    }

    private fun setMappings(mappings: ProjectMapping, version: GameVersion) {
        currentMappings = mappings
        ui.tree.model = DefaultTreeModel(MappingsTreeNode.Root(mappings, ui.treeView.createPackageTree))
        ui.statusManager.currentGameVersion = version
    }

    private fun setAsterisk(asterisk: Boolean) {
        ui.statusManager.hasAsterisk = asterisk
        title = title.substringBeforeLast('*')
        if (asterisk) title += '*'
    }
}
