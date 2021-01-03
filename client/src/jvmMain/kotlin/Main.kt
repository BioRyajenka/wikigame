import GameStateHolder.gameState
import mu.KotlinLogging
import network.NetworkClient
import network.fetchInitialGameState
import network.initializeNetworkEvents
import network.protocol.GameStateDiffEvent
import network.protocol.JoinWorldResponse
import network.protocol.UserActionEvent
import state.gen.GameState
import state.gen.MapState
import state.invoke
import ui.UIEntry
import ui.createInitialDummyPlayer

private val logger = KotlinLogging.logger {}

private suspend fun runLocally(playerId: String) {
    val uiEntry = UIEntry(playerId) { event ->
        val application = event.getApplication(playerId, gameState)
        application.invoke(gameState)
        updateGameWorld()
    }

    uiEntry.init {
        val player = createInitialDummyPlayer()

        gameState = GameState(
            mutableMapOf(),
            mutableMapOf(),
            mutableMapOf(playerId to player),
            MapState(mutableMapOf())
        )
        uiEntry.showGameWorld()
    }
}

private suspend fun runOnline(playerId: String) {
    initializeNetworkEvents()
    val client = NetworkClient { event ->
        logger.info { "Received event $event" }

        when (event) {
            is GameStateDiffEvent -> {
                // TODO: по-другому. там ивент буфер, дифф айди итп
                gameState += event.diff
            }
            is JoinWorldResponse -> {}
            else -> {
                error("Unknown event $event")
            }
        }
    }

    val uiEntry = UIEntry(playerId) { event ->
        // TODO: unreliably
        client.sendReliably(UserActionEvent(event))
    }

    uiEntry.init {
        gameState = fetchInitialGameState(client, playerId)
//         TODO: convert every player's active action start time from server to client time
        uiEntry.showGameWorld()
    }
}

actual suspend fun main() {
//    runLocally("player1")
    runOnline("player1")
}
