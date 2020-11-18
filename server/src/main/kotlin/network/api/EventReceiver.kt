package server.network.api

//import server.network.ClientDeltasMechanism

/**
 * Receives events and schedules them to the player states they are corresponded to
 */

/*class EventReceiver {
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
*/
