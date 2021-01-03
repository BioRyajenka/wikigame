package network

import SERVER_PORT
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse
import state.gen.GameState
import ui.UIEntry

class NetworkEntry(private val client: NetworkClient) {

    suspend fun fetchInitialGameState(playerId: String): GameState {
        client.connect("localhost", SERVER_PORT)

        val joinWorldRequest = JoinWorldRequest(playerId)
        val joinWorldResponse: JoinWorldResponse = client.sendReliablyAndAwait(joinWorldRequest)

        println("World loaded!")

        return joinWorldResponse.initialGameState
    }
}
