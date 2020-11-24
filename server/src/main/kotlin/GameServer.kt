import com.gitlab.mvysny.konsumexml.getValueInt
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.whirvis.jraknet.RakNet
import com.whirvis.jraknet.RakNetPacket
import com.whirvis.jraknet.peer.RakNetClientPeer
import com.whirvis.jraknet.protocol.Reliability
import com.whirvis.jraknet.server.RakNetServer
import com.whirvis.jraknet.server.RakNetServerListener
import network.ClientDiffsMechanism
import network.protocol.GameStateDiffEvent
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse
import network.protocol.NetworkEventManager
import state.*
import state.action.UserAction
import state.entity.PersonalInfo
import state.entity.User
import state.gen.EntityState
import state.gen.GameState
import state.gen.MapState
import state.gen.PlayerState
import java.net.InetSocketAddress
import kotlin.properties.Delegates

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
- сервер кидает UNRELIABLE_WITH_ACK
- ПЕРЕД вычислением дифа происходит "сужение" всего стейта до видимости клиента (но это можно в TODO)

Diff это отдельная сущность или та же? Предпочтительно та же
 */

private fun createInitialGameState(): GameState {
    var width: Int
    var height: Int
    var tileWidth by Delegates.notNull<Int>()
    var tileHeight by Delegates.notNull<Int>()

    val layers = mutableListOf<Map<IntPosition, Int>>()

    GameServer::class.java.getResource("map.tmx").readText().konsumeXml().apply {
        child("map") {
            width = attributes.getValueInt("width")
            height = attributes.getValueInt("height")
            tileWidth = attributes.getValueInt("tilewidth")
            tileHeight = attributes.getValueInt("tileheight")
            val layersNum = attributes.getValueInt("nextlayerid")

            repeat(layersNum) { layers.add(mutableMapOf()) }

            children("layer") {
                val layerId = attributes.getValueInt("id")
                val layerStringData = childText("data")
                println(1 == 1)
            }
        }
    }

    val mapState = MapState(layers.first().keys.associateWith { cellPos ->
        MapCell(layers.map { it.getValue(cellPos) })
    }.toMutableMap())

    val playersPos = listOf(
        IntPosition(5, 5),
        IntPosition(5, 15)
    )
    val entities = mutableListOf<EntityState>()
    entities += playersPos.mapIndexed { i, playerPos ->
        val playerId = "player${i + 1}"
        val playerName = "Player ${i + 1}"

        PlayerState(
            playerId,
            Position(1f * playerPos.j * tileWidth, 1f * playerPos.i * tileHeight),
            VariableWithEmptyValue.empty(),
            VariableWithEmptyValue.empty(),
            PersonalInfo(3f),
            User(playerId, playerName)
        )
    }

    return GameState(entities.associateBy { it.id }.toMutableMap(), mapState)
}

private fun constrictGameState(gameState: GameState, playerId: String): GameState {
    return gameState // TODO
}

private fun filterAffectedPlayers(event: UserAction, players: Collection<Player>): Collection<Player> {
    // FIXME: 21.11.2020 it is temporary function. in future it will be implemented more efficiently
    return players // TODO
}

class GameServer(
    private val server: NetworkServer,
    updateFrequency: Hertz
) : RakNetServerListener {
    private val updatePeriod: Millis = 1000 / updateFrequency
    private val players = mutableMapOf<RakNetClientPeer, Player>()
    private val globalGameState: GameState = createInitialGameState()


    fun start() {
        server.start()

        while (false) {
            update()

            RakNet.sleep(updatePeriod.toLong())
        }
    }

    private fun update() {
        players.forEach { (peer, client) ->
            client.diffsMechanism.retrieveDiffToSend()?.let { (diffToSend, diffId) ->
                val diffEvent = GameStateDiffEvent(diffToSend, diffId)

                server.sendUnreliablyWithAck(peer, diffEvent) {
                    client.diffsMechanism.diffAcknowledged(diffId)
                }
            }
        }
    }

    override fun onDisconnect(server: RakNetServer, address: InetSocketAddress, peer: RakNetClientPeer, reason: String) {
        val player = players.remove(peer)!!

        println("Client ${player.id} disconnected!")

        val playerState = globalGameState.entities.getValue(player.id) as PlayerState
        val application =
            playerState.activeAction!!.getValue()?.onCancelOrFinish(playerState, globalGameState)
        application?.invoke(globalGameState)
    }

    override fun handleMessage(server: RakNetServer, peer: RakNetClientPeer, packet: RakNetPacket, channel: Int) {
        val event = NetworkEventManager.resolveEvent(packet) ?: error("Unknown event")

        if (event.id == JoinWorldRequest.eventId) {
            println("Player ${(event as JoinWorldRequest).playerId} connected")

            val diffsMechanism = ClientDiffsMechanism(globalGameState)
            players[peer] = Player(event.playerId, diffsMechanism)

            val constrictedGameState = constrictGameState(globalGameState, event.playerId)
            val response = JoinWorldResponse(constrictedGameState)
            peer.sendMessage(Reliability.RELIABLE, response.getPreparedPacket())

            return
        }

        val player = players[peer] ?: error("Message from player who didn't join the world")

        if (event is UserAction) {
            // TODO: align event.aroseAtTime to server time

            val playerState = globalGameState.entities.getValue(player.id) as PlayerState
            val application = event.getApplication(playerState, globalGameState)
            val affectedPlayerState = filterAffectedPlayers(event, players.values)

            application.invoke(globalGameState)
            affectedPlayerState.forEach { it.diffsMechanism.apply(application) }

            return
        }

        error("Unknown packet received")
    }
}

class Player(val id: String, val diffsMechanism: ClientDiffsMechanism)
