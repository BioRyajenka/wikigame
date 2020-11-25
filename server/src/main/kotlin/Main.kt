import network.NetworkServer
import network.initializeNetworkEvents

fun main() {
    initializeNetworkEvents()

    val networkServer = NetworkServer(SERVER_PORT, MAX_CONNECTIONS)
    GameServer(networkServer, 20.0).start()
}
