import com.whirvis.jraknet.RakNetPacket
import com.whirvis.jraknet.peer.RakNetClientPeer
import com.whirvis.jraknet.protocol.Reliability
import com.whirvis.jraknet.server.RakNetServer
import network.initializeNetworkEvents
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse
import network.protocol.NetworkEventManager

fun main() {
    initializeNetworkEvents()

    val server = object : RakNetServer(SERVER_PORT, MAX_CONNECTIONS) {
        override fun handleMessage(server: RakNetServer, peer: RakNetClientPeer, packet: RakNetPacket, channel: Int) {
            val event = NetworkEventManager.resolveEvent(packet) ?: error("Unknown event")
            println("Client said ${(event as JoinWorldRequest).requestHi}")

            val response = JoinWorldResponse("hi there")
            peer.sendMessage(Reliability.RELIABLE, response.preparePacket())
        }
    }

    server.start()
}
