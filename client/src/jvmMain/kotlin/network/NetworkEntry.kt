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
        val joinWorldRequest = JoinWorldRequest("request hi")
        val joinWorldResponse: JoinWorldResponse = client.sendReliablyAndAwait(joinWorldRequest)

        println("received ${joinWorldResponse.responseHi}")

//        GameStateHolder.gameState =
    }
}
