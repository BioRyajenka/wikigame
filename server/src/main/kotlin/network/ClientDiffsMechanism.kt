package network

import EMPTY_DIFF_GENERATOR
import mutableDropWhile
import state.gen.GameState
import state.gen.GameStateDiff

/**
 * Store some amount of unacknowledged diffs and current unsent and thus unacknowledged diff.
 *
 * sentUnapprovedDiffs contains already constricted diffs
 * The only place where constriction occurs is before creation of ClientDiffsMechanism
 *
 * note: expansion of view zone is done when we calculate clients
 *       affected by event
 */

class ClientDiffsMechanism(private val globalGameState: GameState) {
    private var sentUnapprovedDiffs: MutableList<Pair<GameStateDiff, Int>> = mutableListOf()
    private var currentDiff: GameStateDiff? = null
    private var freeDiffId: Int = 0

    fun diffAcknowledged(diffId: Int) {
        sentUnapprovedDiffs.mutableDropWhile { it.second != diffId }
        val acknowledged = sentUnapprovedDiffs.removeFirst()

        if (sentUnapprovedDiffs.isEmpty()) {
            check(acknowledged == currentDiff!! to freeDiffId - 1)
            currentDiff = null
        } else {
            currentDiff!! -= acknowledged.first
        }
    }

    fun retrieveDiffToSend(): Pair<GameStateDiff, Int>? {
        if (currentDiff == null) return null

        val result = currentDiff!! to freeDiffId++
        sentUnapprovedDiffs.add(result)
        return result
    }

    fun apply(diffModification: (GameStateDiff) -> Unit) {
        currentDiff = currentDiff ?: EMPTY_DIFF_GENERATOR()
        diffModification.invoke(currentDiff!!)
    }
}
