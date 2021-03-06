package juuxel.enpassantviewer.action

import io.github.cottonmc.proguardparser.ProjectMapping
import javax.swing.JFileChooser
import javax.swing.JFrame
import juuxel.enpassantviewer.ui.status.GameVersion

class ActionContext(
    val frame: JFrame,
    private val getMappings: () -> ProjectMapping,
    private val getGameVersion: () -> GameVersion,
    private val setMappings: (ProjectMapping, GameVersion) -> Unit,
    private val setAsterisk: (Boolean) -> Unit,
    val fileChooser: JFileChooser
) {
    val mappings: ProjectMapping get() = getMappings()
    val gameVersion: GameVersion get() = getGameVersion()

    fun setMappings(mappings: ProjectMapping) =
        setMappingsAndVersion(mappings, this.gameVersion)

    fun setMappingsAndVersion(mappings: ProjectMapping, version: GameVersion) =
        setMappings.invoke(mappings, version)

    fun setAsterisk(hasAsterisk: Boolean) =
        setAsterisk.invoke(hasAsterisk)
}
