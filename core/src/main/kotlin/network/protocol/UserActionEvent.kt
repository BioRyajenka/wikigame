package network.protocol

import com.whirvis.jraknet.RakNetPacket
import state.action.UserAction
import state.gen.readArbitrary
import state.gen.writeArbitrary

class UserActionEvent(val event: UserAction) : NetworkEvent(eventId){

    companion object : NetworkEventCompanion({ packet ->
        val event = readArbitrary<UserAction>(packet)
        UserActionEvent(event)
    }, "UserActionEvent")

    override fun write(packet: RakNetPacket) {
        writeArbitrary(event, packet)
    }
}
