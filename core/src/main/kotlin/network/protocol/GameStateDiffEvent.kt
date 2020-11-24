package network.protocol

import com.whirvis.jraknet.RakNetPacket
import state.gen.GameStateDiff
import state.gen.readGameStateDiff
import state.gen.writeGameStateDiff

class GameStateDiffEvent(val diff: GameStateDiff, val diffId: Int) : NetworkEvent(eventId) {

    companion object : NetworkEventCompanion({ packet ->
        val diff = readGameStateDiff(packet)
        val diffId = packet.readInt()
        GameStateDiffEvent(diff, diffId)
    })

    override fun write(packet: RakNetPacket) {
        writeGameStateDiff(diff, packet)
        packet.writeInt(diffId)
    }
}
