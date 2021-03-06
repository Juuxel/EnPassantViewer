package juuxel.enpassantviewer.ui.status

import com.alee.extended.statusbar.WebStatusBar
import javax.swing.JLabel

class EPStatusBar : WebStatusBar(), StatusManager {
    override var currentGameVersion: GameVersion = GameVersion.Uninitialized
        set(value) {
            field = value
            label.text = getVersionString(value)
        }

    override var hasAsterisk: Boolean = false
        set(value) {
            field = value

            label.text = label.text.substringBeforeLast('*')
            if (value) label.text += '*'
        }

    private val label: JLabel = JLabel(getVersionString(currentGameVersion))

    init {
        add(label)
    }

    private fun getVersionString(version: GameVersion) =
        when (version) {
            is GameVersion.Actual -> version.version
            GameVersion.Uninitialized -> "No mappings loaded"
            GameVersion.Unknown -> "Unknown"
        }
}
