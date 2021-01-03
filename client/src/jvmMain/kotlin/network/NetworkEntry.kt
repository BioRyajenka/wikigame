package network

import SERVER_PORT
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse
import state.gen.GameState
import ui.UIEntry

suspend fun fetchInitialGameState(client: NetworkClient, playerId: String): GameState {
    client.connect("localhost", SERVER_PORT)

    val joinWorldRequest = JoinWorldRequest(playerId)
    val joinWorldResponse: JoinWorldResponse = client.sendReliablyAndAwait(joinWorldRequest)

    println("World loaded!")

    return joinWorldResponse.initialGameState
}
