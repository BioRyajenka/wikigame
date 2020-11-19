import com.whirvis.jraknet.peer.RakNetClientPeer
import com.whirvis.jraknet.protocol.message.EncapsulatedPacket
import com.whirvis.jraknet.protocol.message.acknowledge.Record
import com.whirvis.jraknet.server.RakNetServer
import com.whirvis.jraknet.server.RakNetServerListener
import network.protocol.NetworkEvent

class NetworkServer(port: Int, maxConnections: Int) : RakNetServerListener {
    private val server = RakNetServer(port, maxConnections)

    fun start() {
        server.start()
        server.addListener(this)
    }

    fun addListener(listener: RakNetServerListener) {
        server.addListener(listener)
    }

    override fun onAcknowledge(server: RakNetServer, peer: RakNetClientPeer, record: Record, packet: EncapsulatedPacket) {
        println("hehey")
    }

    fun sendUnreliablyWithAck(peer: RakNetClientPeer, event: NetworkEvent, onAck: () -> Unit) {
//                val packet = diffEvent.preparePacket()
//                val associatedPacket = server.sendMessage(peer, Reliability.UNRELIABLE_WITH_ACK_RECEIPT, packet)
    }


}
