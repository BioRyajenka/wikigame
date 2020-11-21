package network.protocol

import com.whirvis.jraknet.RakNetPacket
import state.GameState
import state.GameStateDiff
import state.MapState
import state.entity.EntityState

private fun readEntities(packet: RakNetPacket): List<EntityState> {
    return (0 until packet.readInt()).map {
        EntityState(packet.readString())
    }
}

private fun writeEntities(packet: RakNetPacket, entities: Collection<EntityState>) {
    packet.writeInt(entities.size)
    entities.forEach { entity ->
        packet.writeString(entity.id)
    }
}

private fun writeMapState(packet: RakNetPacket, mapState: MapState) {

}

private fun readMapState(packet: RakNetPacket) : MapState {
    return MapState()
}

class GameStateDiffEvent(val diff: GameStateDiff, val diffId: Int) : NetworkEvent(eventId) {

    companion object : NetworkEventCompanion({ packet ->
        val diff = GameState(
            readEntities(packet).associateBy { it.id },
            readMapState(packet)
        )
        GameStateDiffEvent(diff)
    })

    override fun write(packet: RakNetPacket) {
        writeEntities(packet, diff.entities.values)
        writeMapState(packet, diff.mapState)
    }
}
