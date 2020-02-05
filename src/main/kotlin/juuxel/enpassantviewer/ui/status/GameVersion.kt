package juuxel.enpassantviewer.ui.status

sealed class GameVersion {
    fun getVersionOrNull(): String? = if (this is Actual) version else null

    companion object {
        operator fun invoke(version: String): GameVersion.Actual =
            GameVersion.Actual(version)
    }

    data class Actual(val version: String) : GameVersion()
    object Unknown : GameVersion()
    object Uninitialized : GameVersion()
}
