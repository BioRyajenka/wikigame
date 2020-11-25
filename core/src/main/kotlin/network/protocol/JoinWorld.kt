package network.protocol

import com.whirvis.jraknet.RakNetPacket
import state.gen.GameState
import state.gen.readGameState
import state.gen.writeGameState

class JoinWorldRequest(val playerId: String) : NetworkEvent(eventId) {
    companion object : NetworkEventCompanion({ packet ->
        JoinWorldRequest(packet.readString())
    })

    override fun write(packet: RakNetPacket) {
        packet.writeString(playerId)
    }
}

class JoinWorldResponse(val initialGameState: GameState) : NetworkEvent(eventId) {
    companion object : NetworkEventCompanion({ packet ->
        JoinWorldResponse(readGameState(packet))
    })

    override fun write(packet: RakNetPacket) {
        writeGameState(initialGameState, packet)
    }
}
