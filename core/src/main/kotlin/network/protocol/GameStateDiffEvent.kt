package network.protocol

import com.whirvis.jraknet.RakNetPacket
import state.*

/*class GameStateDiffEvent(val diff: GameStateDiff, val diffId: Int) : NetworkEvent(eventId) {

    companion object : NetworkEventCompanion({ packet ->
        val diff = GameState(
            readEntities(packet).associateBy { it.id },
            readMapState(packet)
        )
//        GameStateDiffEvent(diff)
        TODO()
    })

    override fun write(packet: RakNetPacket) {
        writeEntities(packet, diff.entities.values)
//        writeMapState(packet, diff.mapState)
    }
}*/
