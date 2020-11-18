package network.protocol

import com.whirvis.jraknet.RakNetPacket

class JoinWorldRequest(val requestHi: String) : NetworkEvent(thisParticularEventId) {
    companion object : NetworkEventCompanion({ packet ->
        JoinWorldRequest(packet.readString())
    })

    override fun write(packet: RakNetPacket) {
        packet.writeString(requestHi)
    }
}

class JoinWorldResponse(val responseHi: String) : NetworkEvent(thisParticularEventId) {
    companion object : NetworkEventCompanion({ packet ->
        JoinWorldResponse(packet.readString())
    })

    override fun write(packet: RakNetPacket) {
        packet.writeString(responseHi)
    }
}
