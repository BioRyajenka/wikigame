import mu.KotlinLogging
import network.ClientDiffsMechanism
import network.ClientPeer
import network.NetworkServer
import network.ServerEventListener
import network.protocol.GameStateDiffEvent
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse
import network.protocol.NetworkEvent
import state.Millis
import state.action.Move
import state.action.UserAction
import state.gen.GameState
import state.gen.PlayerStateDiff
import state.invoke

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

private val logger = KotlinLogging.logger {}

private fun constrictGameState(gameState: GameState, playerId: String): GameState {
    return gameState // TODO
}

private fun filterAffectedPlayers(event: UserAction, clients: Collection<Client>): Collection<Client> {
    return clients // TODO
}

class GameServer(
    private val server: NetworkServer,
    private val updatePeriod: Millis
) : ServerEventListener {
    private val clients = mutableMapOf<ClientPeer, Client>()
    private val globalGameState: GameState = createInitialGameState()


    fun start() {
        server.addListener(this)
        server.start()

        while (true) {
            Thread.sleep(updatePeriod.toLong())

            //deleteme
//            if (clients.isNotEmpty()) {
//                val (peer, client) = clients.entries.single()
//                val player2 = globalGameState.getPlayer("player2")
//                processUserAction("player2", Move(
//                    TimeProvider.currentTime, player2.position.copy { x += 500 }
//                ))
//            }

            update()
        }
    }

    private fun update() {
        clients.forEach { (peer, client) ->
            client.diffsMechanism.retrieveDiffToSend()?.let { (diffToSend, diffId) ->
                val diffEvent = GameStateDiffEvent(diffToSend, diffId)

                peer.sendUnreliablyWithAck(diffEvent) {
                    client.diffsMechanism.diffAcknowledged(diffId)
                }
            }
        }
    }

    override fun onDisconnect(peer: ClientPeer) {
        val client = clients.remove(peer)!!

        println("Client ${client.id} disconnected!")

        val player = globalGameState.players[client.id]!!
        val disconnectingTime = TimeProvider.currentTime

        val cancelOrFinishApplication = player.activeAction.getValue()?.getOnCancelOrFinish(
            globalGameState, player.id, disconnectingTime
        )
        cancelOrFinishApplication?.invoke(globalGameState)
    }

    override fun handleMessage(peer: ClientPeer, event: NetworkEvent) {
        if (event.id == JoinWorldRequest.eventId) {
            println("Player ${(event as JoinWorldRequest).playerId} connected")

            val diffsMechanism = ClientDiffsMechanism(globalGameState)
            clients[peer] = Client(event.playerId, diffsMechanism)

            val constrictedGameState = constrictGameState(globalGameState, event.playerId)
            peer.sendReliably(JoinWorldResponse(constrictedGameState))

            return
        }

        val client = clients[peer] ?: error("Message from player who didn't join the world")

        if (event is UserAction) { // TODO: replace with id comparison
            if (checkUserActionValidity(client.id, event)) {
                processUserAction(client.id, event)
            } else {
                logger.warn { "User ${client.id} sent an invalid action $event" }
            }

            return
        }

        error("Unknown packet received")
    }

    private fun checkUserActionValidity(initiatorId: String, event: UserAction): Boolean {
        val initiator = globalGameState.players[initiatorId]!!

        when (event) {
            is Move -> {
                // TODO: maybe eps comparison?
                if (initiator.personalInfo.movementSpeed != event.speed) {
                    return false
                }
                //  Move event не должен быть на слишком большую дистанцию
                //  (не больше скольки-то игровых экранов)
            }
        }

        return true
    }

    // TODO: "player logged in" action
    private fun processUserAction(initiatorId: String, event: UserAction) {
        // TODO: align event.aroseAtTime to server time (correctly? with offset?)
        event.aroseAtTime = TimeProvider.currentTime

        // TODO: add player to every affected player's diff before action application

        val initiator = globalGameState.players[initiatorId]!!

        val application = event.getApplication(initiator.id, globalGameState)
        val affectedPlayerStates = filterAffectedPlayers(event, clients.values)

        // propagate info
        affectedPlayerStates.forEach { affectedPlayer ->
            affectedPlayer.diffsMechanism.apply { affectedDiff ->
                if (initiatorId !in affectedDiff.players) {
                    // TODO: propagate not every time when diff doesn't contain player,
                    //       but strongly when we send player for the first time
                    affectedDiff.players[initiator.id] = PlayerStateDiff(
                        initiator.id,
                        initiator.position,
                        initiator.activeAction,
                        initiator.speechState,
                        null,
                        initiator.publicInfo
                    )
                }
            }
        }

        application.invoke(globalGameState)
        affectedPlayerStates.forEach { it.diffsMechanism.apply(application) }
    }
}

class Client(val id: String, val diffsMechanism: ClientDiffsMechanism)
