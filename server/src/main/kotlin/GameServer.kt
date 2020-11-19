import com.whirvis.jraknet.RakNet
import com.whirvis.jraknet.RakNetPacket
import com.whirvis.jraknet.peer.RakNetClientPeer
import com.whirvis.jraknet.protocol.Reliability
import com.whirvis.jraknet.server.RakNetServer
import com.whirvis.jraknet.server.RakNetServerListener
import network.ClientDeltasMechanism
import network.EventBuffer
import network.protocol.GameStateDiffEvent
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse
import network.protocol.NetworkEventManager
import state.GameState
import state.Hertz
import state.MapState
import state.Millis
import state.action.UserAction
import state.entity.EntityState
import java.net.InetSocketAddress

/**
) дифы могут реализовывать операцию +
если дифф аддитивный, то есть два способа как поступить в случае, если клиенту пришел дифф на ивент, который уже был аппрувнут:
а) просто дропнуть дифф. тут получается недостаток по сравнению с неаддитивным диффом
б) хранить несколько последних дифов и на клиенте. т.е. использовать DeltasMechanism и там и там
) дифы могут реализовывать операцию -
тогда на стороне сервера в DeltasMechanism после ACK можно как заново поверх применять ивенты, так и "вычитать" с левого конца. второе кажется выгоднее
) дифы могут не реализовывать ни + ни -
если дифф не аддитивный, то "новый стейт = дифф". т.е. в диффе не "аддитивность", а новое значение полей, которые нужно перезаписать
этот вариант используется в квейке
) дифы могут реализовывать и то и то
нужно ли это рассматривать?

еще есть вариант С EventBuffer или БЕЗ. во втором случае можно просто reliable_ordered использовать
(непонятно что быстрее)

вариантов реализации много, сейчас был выбран такой:
 - идемпотентные ("декларативные") дифы
 - без EventBuffer
 - клиент кидает UNRELIABLE_ORDERED
 - сервер кидает UNRELIABLE_SEQUENCED_WITH_ACK
 - ПЕРЕД вычислением дифа происходит "сужение" всего стейта до видимости клиента (но это можно в TODO)
 */

class GameServer(
    private val server: NetworkServer,
    updateFrequency: Hertz
) : RakNetServerListener {
    private val updatePeriod: Millis = 1000 / updateFrequency
    private val clients = mutableMapOf<RakNetClientPeer, Client>()
    private val globalGameState: GameState = createGameState()

    private fun createGameState(): GameState {
        val entities = mutableListOf<EntityState>()
        val mapState = MapState()

        return GameState(entities.associateBy { it.id }, mapState)
    }


    fun start() {
        server.start()

        while (false) {
            update()

            RakNet.sleep(updatePeriod.toLong())
        }
    }

    private fun update() {
        clients.forEach { (peer, client) ->
            val deltaToSend = client.deltasMechanism.retrieveDeltaToSend()
            if (deltaToSend != null) {
                val diffEvent = GameStateDiffEvent(deltaToSend)

                server.sendUnreliablyWithAck(peer, diffEvent) {
                    client.deltasMechanism.deltaAcknowledged(diffEvent.id)
                }

            }
        }
    }

    override fun onDisconnect(server: RakNetServer, address: InetSocketAddress, peer: RakNetClientPeer, reason: String) {
        println("Client disconnected! Clients before: ${clients.size}")
        clients.remove(peer)
        println("Clients after: ${clients.size}")
    }

    override fun handleMessage(server: RakNetServer, peer: RakNetClientPeer, packet: RakNetPacket, channel: Int) {
        val event = NetworkEventManager.resolveEvent(packet) ?: error("Unknown event")

        if (event.id == JoinWorldRequest.eventId) {
            println("Client said ${(event as JoinWorldRequest).requestHi}")

            val deltasMechanism = ClientDeltasMechanism(globalGameState)
            val eventBuffer = EventBuffer(updatePeriod, deltasMechanism::apply)
            clients[peer] = Client(eventBuffer, deltasMechanism)

            val response = JoinWorldResponse("hi there")
            peer.sendMessage(Reliability.RELIABLE, response.preparePacket())

            return
        }

        val client = clients[peer] ?: error("Message from client who didn't join the world")

        if (event is UserAction) {
            client.eventBuffer.scheduleEvent(event)
            return
        }

        error("Unknown packet received")
    }
}

class Client(val eventBuffer: EventBuffer, val deltasMechanism: ClientDeltasMechanism)
