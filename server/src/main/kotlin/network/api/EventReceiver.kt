package server.network.api

import core.state.action.UserAction
import server.network.ClientDeltasMechanism

/**
 * Receives events and schedules them to the player states they are corresponded to
 *
 * @author Igor Sushencev
 * @since 28.01.19
 * Copyright (c) Huawei Technologies Co., Ltd. 2015-2019. All rights reserved.
 */

class EventReceiver {
    private val clients: MutableList<ClientDeltasMechanism>

    private fun filterAffectedStates(event: UserAction): List<ClientDeltasMechanism> {
        // TODO: temporary we treat all states as affected
        return clients
    }

    fun receive(event: UserAction) {
        val affectedStates = filterAffectedStates(event)
        affectedStates.forEach { it.scheduleEvent(event) }
    }
}
