package network

import com.whirvis.jraknet.RakNetPacket
import com.whirvis.jraknet.peer.RakNetClientPeer
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

    private var ackAwaiters: MutableMap<EncapsulatedPacket, () -> Unit> = mutableMapOf()

    init {
        server.addListener(this)
    }

    fun start() = server.start()

    fun addListener(listener: ServerEventListener) {
        listeners += listener
    }

    override fun onDisconnect(server: RakNetServer, address: InetSocketAddress, peer: RakNetClientPeer, reason: String) {
        val clientPeer = clients.remove(peer)
        if (clientPeer != null) {
            listeners.forEach { it.onDisconnect(clientPeer) }
        }
    }

    override fun onAcknowledge(server: RakNetServer, peer: RakNetClientPeer, record: Record, packet: EncapsulatedPacket) {
        ackAwaiters[packet]?.invoke() ?: error("Acknowledging unregistered awaiter??")
    }

    override fun onLoss(server: RakNetServer, peer: RakNetClientPeer, record: Record, packet: EncapsulatedPacket) {
        ackAwaiters.remove(packet)
    }

    override fun handleMessage(server: RakNetServer, peer: RakNetClientPeer, packet: RakNetPacket, channel: Int) {
        val clientPeer = clients[peer] ?: registerClient(peer)
        val event = NetworkEventManager.resolveEvent(packet) ?: error("Unknown event")

        listeners.forEach { it.handleMessage(clientPeer, event) }
    }

    override fun onHandlerException(server: RakNetServer, address: InetSocketAddress, throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun registerClient(peer: RakNetClientPeer): ClientPeer {
        val client = ClientPeer(peer) { marker: EncapsulatedPacket, onAck ->
            ackAwaiters[marker] = onAck
        }
        clients[peer] = client
        return client
    }
}

interface ServerEventListener {
    fun onDisconnect(peer: ClientPeer)

    fun handleMessage(peer: ClientPeer, event: NetworkEvent)
}

typealias AckAwaiterRegisterer = (marker: EncapsulatedPacket, onAck: () -> Unit) -> Unit

class ClientPeer(private val peer: RakNetClientPeer, private val registerAckAwaiter: AckAwaiterRegisterer) {

    fun sendReliably(event: NetworkEvent) {
        peer.sendMessage(Reliability.RELIABLE, event.getPreparedPacket())
    }

    fun sendUnreliablyWithAck(event: NetworkEvent, onAck: () -> Unit) {
        val packet = event.getPreparedPacket()
        val associatedPacket = peer.sendMessage(Reliability.UNRELIABLE_WITH_ACK_RECEIPT, packet)

        registerAckAwaiter(associatedPacket, onAck)
    }
}
