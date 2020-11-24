package state.action

import state.*
import mu.KotlinLogging
import state.gen.*

private val logger = KotlinLogging.logger {}

sealed class UserAction {
    abstract val actionId: Int // incrementing id. todo: make it autoincrement
    abstract var aroseAtTime: Millis

    /**
     * Apply action to both diff and globalState.
     *
     * globalState is needed for global state retrieval
     * diff.playerStates also contains player
     *
     */
    fun getApplication(playerId: String, globalState: ImmutableGameState) = { diff: GameStateDiff ->
        val o = object: ApplicationHelper {
            override val diffPlayer by lazy {
                diff.entities.computeIfAbsent(player.id) { EntityStateDiff(player.id, null) }!! as PlayerStateDiff
            }
        }

        /**
         * Возможны 2 варианта реализации сетевого протокола, я выбрал второй
         * 1) как в quake: для каждого клиента храним n версий всего мира, которые
         *    используются в ClientDiffsMechanism. При этом Action применяется один
         *    раз ко всему миру, а сужение и создание дифа происходят перед отправкой
         * 2) храним именно отправленные дифы. При этом Action возвращает сразу Diff
         *    (или модифицирующую дифф функцию, что одно и то же), и в качестве параметра
         *    принимает global immutable. Тогда сужение происходит прямо в функции
         *    применения UserAction. Сейчас я реализую с явными проверками в коде, но
         *    в идеале надо рассматривать все поля стейт-дифа как ресурсы и предоставлять
         *    или не предоставлять к ним доступ. Т.е. если нет доступа, то дифф не меняем
         */

        with(o, getApplication())
//        kek()(o)
    }

    interface ApplicationHelper {
        val diffPlayer: PlayerStateDiff
    }

    abstract fun getApplication(): ApplicationHelper.() -> Unit
}

/**
 * Active action is something continuous that cannot exists simultaneously with any other
 * active action for the player. It can be: moving, chopping down the tree etc.
 *
 * onCancelOrFinish fires before switching to another active action and after player disconnects.
 */
sealed class ActiveUserAction : UserAction() {
    override fun getApplication(): ApplicationHelper.() -> Unit = {

    }

    abstract fun onCancelOrFinish(): ApplicationHelper.() -> Unit
}

class CancelActiveAction(
    override val actionId: Int, override var aroseAtTime: Millis
) : ActiveUserAction() {
    override fun getApplication(): ApplicationHelper.() -> Unit = {
        diffPlayer.activeAction = VariableWithEmptyValue.empty()
    }

    override fun onCancelOrFinish(player: ImmutablePlayerState, globalState: ImmutableGameState) = { _ : GameStateDiff ->
        // do nothing
    }
}

// authoritative action
// represents movement with constant speed (if speed changes, need another Move event)
class Move(
    override val actionId: Int, override var aroseAtTime: Millis,
    private val endPosition: Position
) : ActiveUserAction() {
    companion object {
        private fun calculateNewPosition(spatialState: SpatialState, speed: Speed, elapsedTime: Millis): Vector {
            check(elapsedTime >= 0)
            val expectedTime = (spatialState.endPosition - spatialState.startPosition).vectorLength() / speed
            return if (expectedTime < elapsedTime) {
                spatialState.endPosition
            } else {
                spatialState.startPosition + (spatialState.endPosition - spatialState.startPosition) * (elapsedTime / expectedTime)
            }
        }
    }

    весь мув это эктив экшн

    override fun apply(playerState: PlayerState, globalState: GameState) = { diff: GameState ->
        написать это более эффективно, вынеся бОльшую часть за лямбду

        val startPosition = calculateNewPosition(
            playerState.spatialState!!,
            playerState.spatialState!!.speed,
            aroseAtTime - playerState.spatialState!!.movementStartedAtServerTime
        )
        val newSpatialState = SpatialState(
            startPosition,
            endPosition,
            playerState.personalInfo.movementSpeed,
            aroseAtTime
        )

        diffPlayerState.spatialState = newSpatialState
    }
}

/*class DropInventoryItem(
    override val actionId: Int, override var aroseAtTime: Millis,
    val itemId: String, val amount: Int
) : UserAction() {
    private fun removeItems(inventoryState: InventoryState) {
        inventoryState.items.compute(itemId) { _, v ->
            check(v != null && v >= amount)
            v - amount
        }
    }

    override fun apply(diff: GameState, globalState: GameState) {
        removeItems(globalState.playerState.inventory)
        removeItems(diff.playerState.inventory)
    }
}

class PickUpInventoryItem(
    override val actionId: Int, override var aroseAtTime: Millis,
    private val entityId: String
) : UserAction() {
    private fun addItems(inventoryState: InventoryState, itemId: String, amount: Int) {
        check(amount > 0)
        inventoryState.items.compute(itemId) { _, v ->
            (v ?: 0) + amount
        }
    }

    override fun apply(diff: GameState, globalState: GameState) {
        val entity = globalState.groundEntities[entityId]
        checkNotNull(entity) // excessive checks, because of validation
        check(entity is ItemsStackEntity && entity.id == entityId)
        addItems(diff.playerState.inventory, entity.itemId, entity.amount)
        addItems(globalState.playerState.inventory, entity.itemId, entity.amount)
    }
}

// we can show trading icon
class TradeRequest(
    override val actionId: Int, override var aroseAtTime: Millis,
    private val withPlayerId: String
) : UserAction() {
    override fun apply(diff: GameState, globalState: GameState) {
        // TODO: this is totally temporary implementation for debug purposes
        // TODO: in future it is necessary not to forget to modify that player state
        if (globalState.playerState.tradeState.state == TradeState.TradeStateAcceptanceState.TRADING) {
            val timeElapsed: Millis = globalState.playerState.tradeState.tradeStartedAt!!
            if (timeElapsed < 5_000) {
                logger.debug("Trade started ${timeElapsed / 1000} seconds ago, please wait.")
                return
            }
        }
        with(globalState.playerState.tradeState) {
            tradeStartedAt = aroseAtTime
            state = TradeState.TradeStateAcceptanceState.TRADING
        }

        logger.debug("Trading with $withPlayerId")
    }
}

class AcceptTrade(
    override val actionId: Int, override var aroseAtTime: Millis,
    val withPlayerId: String
) : UserAction() {
    override fun apply(diff: GameState, globalState: GameState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// also used to cancel active unaccepted request
class DiscardTrade(
    override val actionId: Int, override var aroseAtTime: Millis,
    val withPlayerId: String
) : UserAction() {
    override fun apply(diff: GameState, globalState: GameState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class SaySomething(
    override val actionId: Int, override var aroseAtTime: Millis,
    val text: String
) : UserAction() {
    override fun apply(diff: GameState, globalState: GameState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
*/
