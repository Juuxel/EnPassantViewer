package juuxel.enpassantviewer.ui.progress

interface StepManager {
    /**
     * The current top entry of the step stack.
     */
    var step: String

    fun pushStep(step: String)
    fun popStep()
}
