package network

import com.whirvis.jraknet.RakNetPacket
import com.whirvis.jraknet.peer.RakNetClientPeer
import com.whirvis.jraknet.protocol.ConnectionType
import com.whirvis.jraknet.protocol.Reliability
import com.whirvis.jraknet.protocol.message.EncapsulatedPacket
import com.whirvis.jraknet.protocol.message.acknowledge.Record
import com.whirvis.jraknet.server.RakNetServer
import com.whirvis.jraknet.server.RakNetServerListener
import network.protocol.NetworkEvent
import network.protocol.NetworkEventManager
import java.net.InetSocketAddress

class NetworkServer(port: Int, maxConnections: Int) : RakNetServerListener {
    private val server = RakNetServer(port, maxConnections)
    private val listeners: MutableList<ServerEventListener> = mutableListOf()
    private val clients: MutableMap<RakNetClientPeer, ClientPeer> = mutableMapOf()

    init {
        server.addListener(this)
    }

    fun start() = server.start()

    fun addListener(listener: ServerEventListener) {
        listeners += listener
    }

    override fun onLogin(server: RakNetServer, peer: RakNetClientPeer) {
        clients[peer] = ClientPeer(peer)
    }

    override fun onDisconnect(server: RakNetServer, address: InetSocketAddress, peer: RakNetClientPeer, reason: String) {
        val clientPeer = clients.remove(peer)
        if (clientPeer != null) {
            listeners.forEach { it.onDisconnect(clientPeer) }
        }
    }

    override fun onAcknowledge(server: RakNetServer, peer: RakNetClientPeer, record: Record, packet: EncapsulatedPacket) {
        println("hehey")
    }

    override fun handleMessage(server: RakNetServer, peer: RakNetClientPeer, packet: RakNetPacket, channel: Int) {
        val clientPeer = clients[peer]!!
        val event = NetworkEventManager.resolveEvent(packet) ?: error("Unknown event")

        listeners.forEach { it.handleMessage(clientPeer, event) }
    }
}

interface ServerEventListener {
    fun onDisconnect(peer: ClientPeer)

    fun handleMessage(peer: ClientPeer, event: NetworkEvent)
}

class ClientPeer(private val peer: RakNetClientPeer) {

    fun sendReliably(event: NetworkEvent) {
        peer.sendMessage(Reliability.RELIABLE, event.getPreparedPacket())
    }

    fun sendUnreliablyWithAck(event: NetworkEvent, onAck: () -> Unit) {

//                val packet = diffEvent.preparePacket()
//                val associatedPacket = server.sendMessage(peer, Reliability.UNRELIABLE_WITH_ACK_RECEIPT, packet)
    }
}
