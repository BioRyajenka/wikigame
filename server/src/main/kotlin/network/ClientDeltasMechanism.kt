package server.network

import state.GameState
import core.state.action.UserAction
import core.world.Millis

/**
 * Store some amount of unacknowledged diffs and current unsended and thus unacknowledged diff.
 * For each diff also cache its serialized version.
 * Also stores one global diff (diff from session's start) for this client
 *
 * @author Igor Sushencev
 * @since 28.01.19
 * Copyright (c) Huawei Technologies Co., Ltd. 2015-2019. All rights reserved.
 */

private typealias EventId = Int

// TODO: how about remove EMPTY_STATE and treat the whole world's state as first diff?
private val EMPTY_STATE: GameState

class ClientDeltasMechanism(relaxationPeriod: Millis, private val globalState: GameState) {
    private val eventBuffer = EventBuffer(relaxationPeriod, ::apply)

    private val sentUnapprovedDeltas = mutableListOf<Pair<GameState, List<EventId>>>()
    private var currentDelta: GameState?
    private val currentEventIdsList = mutableListOf<EventId>()

    fun scheduleEvent(event: UserAction) {
        eventBuffer.scheduleEvent(event)
    }

    fun deltaAcknowledged(eventId: EventId) {

    }

    fun retrieveDeltaToSend(): GameState? {
        if (currentDelta == null) return null
        sentUnapprovedDeltas.add(currentDelta!! to currentEventIdsList)
    }

    private fun apply(event: UserAction) {
        // TODO: modify event.aroseAtTime time to make it server-time
        currentDelta = currentDelta ?: EMPTY_STATE
        event.apply(currentDelta!!, globalState)
        currentEventIdsList.add(event.actionId)
    }
}
