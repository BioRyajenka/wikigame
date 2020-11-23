package network.protocol

import com.whirvis.jraknet.RakNetPacket

class JoinWorldRequest(val playerId: String) : NetworkEvent(eventId) {
    companion object : NetworkEventCompanion({ packet ->
        JoinWorldRequest(packet.readString())
    })

    override fun write(packet: RakNetPacket) {
//        packet.writeString(requestHi)
    }
}

class JoinWorldResponse(/*val initialGameState: GameState*/) : NetworkEvent(eventId) {
    companion object : NetworkEventCompanion({ packet ->
//        JoinWorldResponse(packet.readString())
        TODO()
    })

    override fun write(packet: RakNetPacket) {
//        packet.writeString(responseHi)
    }
}
