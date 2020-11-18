package network.protocol

import com.whirvis.jraknet.RakNetPacket

object NetworkEventManager {
    private val events = mutableMapOf<Short, (RakNetPacket) -> NetworkEvent>()

    fun registerNewEventType(id: Short, eventConstructor: (RakNetPacket) -> NetworkEvent) {
        events += id to eventConstructor
    }

    fun resolveEvent(packet: RakNetPacket): NetworkEvent? {
        return events[packet.id]?.invoke(packet)
    }
}

abstract class NetworkEvent {
    private val packet: RakNetPacket
    private var packetPrepared = false

    constructor(id: Short) {
        require(id in RakNetPacket.ID_USER_PACKET_ENUM..255) {
            "Packet ID must be in between " + RakNetPacket.ID_USER_PACKET_ENUM + "-255"
        }

        this.packet = RakNetPacket(id.toInt())
    }

    constructor(packet: RakNetPacket) {
        this.packet = packet
    }

    fun preparePacket(): RakNetPacket {
        check(!packetPrepared)
        write(packet)
        packetPrepared = true
        return packet
    }

    abstract fun write(packet: RakNetPacket)
}

private var freeId = RakNetPacket.ID_USER_PACKET_ENUM

open class NetworkEventCompanion(read: (RakNetPacket) -> NetworkEvent) {
    val thisParticularEventId = freeId++

    init {
        NetworkEventManager.registerNewEventType(thisParticularEventId, read)
    }
}

