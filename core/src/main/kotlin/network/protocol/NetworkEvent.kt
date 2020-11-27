package network.protocol

import com.whirvis.jraknet.RakNetPacket

object NetworkEventManager {
    private val events = mutableMapOf<Short, Pair<(RakNetPacket) -> NetworkEvent, String>>()

    fun registerNewEventType(id: Short, nameForDebug: String, eventConstructor: (RakNetPacket) -> NetworkEvent) {
        events += id to (eventConstructor to nameForDebug)
    }

    fun resolveEvent(packet: RakNetPacket): NetworkEvent? {
        return events[packet.id]?.first?.invoke(packet)
    }
}

abstract class NetworkEvent {
    val id: Short
        get() = packet.id

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

    fun getPreparedPacket(): RakNetPacket {
        if (packetPrepared) return packet;
        write(packet)
        packetPrepared = true
        return packet
    }

    protected abstract fun write(packet: RakNetPacket)
}

private var freeId: Short = 150 //RakNetPacket.ID_USER_PACKET_ENUM

open class NetworkEventCompanion(read: (RakNetPacket) -> NetworkEvent, nameForDebug: String) {
    val eventId = freeId++

    init {
        NetworkEventManager.registerNewEventType(eventId, nameForDebug, read)
    }
}

