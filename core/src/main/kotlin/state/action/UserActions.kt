package state.action

import generation.TransferableViaNetwork
import mu.KotlinLogging
import state.*
import state.gen.*

private val logger = KotlinLogging.logger {}

/**
 * Храним именно отправленные дифы. При этом Action возвращает сразу Diff
 * (или модифицирующую дифф функцию, что одно и то же), и в качестве параметра
 * принимает global immutable. Тогда сужение происходит прямо в функции
 * применения UserAction. Сейчас я реализую с явными проверками в коде, но
 * в идеале надо рассматривать все поля стейт-дифа как ресурсы и предоставлять
 * или не предоставлять к ним доступ. Т.е. если нет доступа, то дифф не меняем
 *
 * Это также имеет смысл чтобы валидировать что поля дифа не обнуляются (такое невалидно)
 * Типа если было diff.a = smth, то нельзя его делать null
 *
 * А еще это имеет смысл чтобы автоматически расширять дифф: если у юзера есть доступ
 * к ресурсу И сейчас поле null, то берем значение из глобала
 */

sealed class UserAction {
    abstract var aroseAtTime: Millis

    interface AppGlobalHelper {
        val initiatorGlobal: ImmutablePlayerState
    }

    interface AppDiffHelper {
        var nullableInitiator: PlayerStateDiff?

        val notNullInitiator: PlayerStateDiff
    }

    /**
     * Apply action to both diff and globalState.
     *
     * globalState is needed for global state retrieval
     * diff.playerStates also contains player
     *
     */
    fun getApplication(initiatorId: String, globalState: ImmutableGameState): (diff: GameStateDiff) -> Unit {
        val globalHelper = createGlobalHelper(globalState, initiatorId)

        val application = globalHelper.getApplication()

        return { diff -> application(createDiffHelper(diff, initiatorId)) }
    }

    abstract fun AppGlobalHelper.getApplication(): AppDiffHelper.() -> Unit
}

private fun createGlobalHelper(
    globalState: ImmutableGameState,
    initiatorId: String,
): UserAction.AppGlobalHelper {
    return object : UserAction.AppGlobalHelper {
        override val initiatorGlobal = globalState.players[initiatorId]!!
    }
}

private fun createDiffHelper(diff: GameStateDiff, initiatorId: String): UserAction.AppDiffHelper {
    return object : UserAction.AppDiffHelper {
        override var nullableInitiator = diff.players[initiatorId]
            set(value) {
                check(field == null) {
                    "Resetting this field is not allowed"
                }
                field = value
                diff.players[initiatorId] = value
            }
        override val notNullInitiator: PlayerStateDiff
            get() {
                if (nullableInitiator == null) {
                    nullableInitiator = PlayerStateDiff(
                        initiatorId, null, null,
                        null, null, null
                    )
                }
                return nullableInitiator!!
            }
    }
}

/**
 * Active action is something continuous that cannot exists simultaneously with any other
 * active action for the player. It can be: moving, chopping down the tree etc.
 *
 * onCancelOrFinish fires before switching to another active action and after player disconnects.
 */
sealed class ActiveUserAction : UserAction() {
    final override fun AppGlobalHelper.getApplication(): AppDiffHelper.() -> Unit {
        val onCancelOrFinishApp = if (!initiatorGlobal.activeAction.empty()) {
            with(initiatorGlobal.activeAction.getValue()!!) {
                getOnCancelOrFinish(this@ActiveUserAction.aroseAtTime)
            }
        } else null

        val onAttachApp = getOnAttach()

        return {
            onCancelOrFinishApp?.invoke(this)

            // 2. starting new
            notNullInitiator.activeAction = VariableWithEmptyValue.ofValue(this@ActiveUserAction)
            onAttachApp()
        }
    }

    /**
     * Action is guaranteed to be not cancelled when calling these functions
     */
    protected abstract fun AppGlobalHelper.getOnAttach(): AppDiffHelper.() -> Unit
    protected abstract fun AppGlobalHelper.getOnCancelOrFinish(currentTime: Millis): AppDiffHelper.() -> Unit

    fun getOnCancelOrFinish(globalState: ImmutableGameState, initiatorId: String, currentTime: Millis): (GameStateDiff) -> Unit {
        val gHelper = createGlobalHelper(globalState, initiatorId)
        return { diff ->
            val dHelper = createDiffHelper(diff, initiatorId)
            val application = with(gHelper) { getOnCancelOrFinish(currentTime) }
            dHelper.application()
        }
    }
}

@TransferableViaNetwork
class CancelActiveAction(override var aroseAtTime: Millis) : UserAction() {
    override fun AppGlobalHelper.getApplication(): AppDiffHelper.() -> Unit = {
        notNullInitiator.activeAction = VariableWithEmptyValue.empty()
    }
}

// represents movement with constant speed (if speed changes, need another Move event)
// if movement distance is large, also need another Move event
@TransferableViaNetwork
class Move(override var aroseAtTime: Millis, val speed: Speed, val endPosition: Position) : ActiveUserAction() {

    fun calculateNewPosition(player: ImmutableMobState, currentTime: Millis): Vector {
        val startPosition = player.position
        val elapsedTime = currentTime - aroseAtTime

        check(elapsedTime >= 0)
        val expectedTime = (endPosition - startPosition).vectorLength / speed
        return if (expectedTime < elapsedTime) {
            endPosition
        } else {
            startPosition + (endPosition - startPosition) * (elapsedTime / expectedTime)
        }
    }

    override fun AppGlobalHelper.getOnAttach(): AppDiffHelper.() -> Unit {
        return {
            // do nothing - active action already attached
            1 == 1 // for debug
        }
    }

    override fun AppGlobalHelper.getOnCancelOrFinish(currentTime: Millis): AppDiffHelper.() -> Unit {
        val previousInitiatorAction = initiatorGlobal.activeAction.getValue()
        val newPos = if (previousInitiatorAction is Move) {
            previousInitiatorAction.calculateNewPosition(initiatorGlobal, currentTime)
        } else null

        return {
            if (newPos != null) notNullInitiator.position = newPos
        }
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
