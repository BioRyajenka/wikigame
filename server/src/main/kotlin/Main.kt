import network.NetworkServer
import network.initializeNetworkEvents
import state.hz

fun main() {
    initializeNetworkEvents()

    val networkServer = NetworkServer(SERVER_PORT, MAX_CONNECTIONS)
    GameServer(networkServer, 20.hz).start()
}
