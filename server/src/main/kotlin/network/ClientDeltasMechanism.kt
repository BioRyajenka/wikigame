package network

import state.GameState
import state.Millis
import state.action.UserAction

/**
 * Store some amount of unacknowledged diffs and current unsent and thus unacknowledged diff.
 * For each diff also cache its serialized version.
 * Also stores one global diff (diff from session's start) for this client
 */


private typealias EventId = Int

//private val EMPTY_STATE = GameState()

class ClientDeltasMechanism(private val globalState: GameState) {
    private val sentUnapprovedDeltas = mutableListOf<Pair<GameState, List<EventId>>>()
//    private var currentDelta: GameState?
    private val currentEventIdsList = mutableListOf<EventId>()

    fun deltaAcknowledged(eventId: EventId) {

    }

    fun retrieveDeltaToSend(): GameState? {
        TODO()
//        if (currentDelta == null) return null
//        sentUnapprovedDeltas.add(currentDelta!! to currentEventIdsList)
    }

    fun apply(event: UserAction) {
        // TODO: modify event.aroseAtTime time to make it server-time
//        currentDelta = currentDelta ?: EMPTY_STATE
//        event.apply(currentDelta!!, globalState)
//        todo: куда еще применяем?
//        currentEventIdsList.add(event.actionId)
    }
}
