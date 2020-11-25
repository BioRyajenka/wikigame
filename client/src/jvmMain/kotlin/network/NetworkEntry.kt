package network

import SERVER_PORT
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse

actual object NetworkEntry {
    lateinit var client: NetworkClient

    actual suspend operator fun invoke() {
        initializeNetworkEvents()

        client = NetworkClient()

        client.connect("localhost", SERVER_PORT)

        val joinWorldRequest = JoinWorldRequest("player1")
        val joinWorldResponse: JoinWorldResponse = client.sendReliablyAndAwait(joinWorldRequest)

        GameStateHolder.gameState = joinWorldResponse.initialGameState

        println("World loaded!")
    }
}
