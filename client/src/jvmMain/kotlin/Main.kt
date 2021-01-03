import GameStateHolder.gameState
import network.NetworkClient
import network.initializeNetworkEvents
import state.gen.GameState
import state.gen.MapState
import state.invoke
import ui.UIEntry
import ui.createInitialDummyPlayer

actual suspend fun main() {
    val playerId = "player1"

    initializeNetworkEvents()
    val client = NetworkClient()

    val uiEntry = UIEntry(playerId) { event ->
//        client.sendReliably()
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

//    async(coroutineContext) { or GlobalScope.launch
//        val gameState = NetworkEntry(client).fetchInitialGameState(playerId)
        // TODO: convert every player's active action start time from server to client time
//        uiEntry.showGameWorld()
//    }
    }
}
