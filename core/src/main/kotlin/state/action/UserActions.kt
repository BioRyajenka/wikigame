package state.action

import state.*
import state.entity.PlayerState
import state.GameState
import mu.KotlinLogging

/**
 * There are two "domain types" of state: authoritative and cumulative.
 * The first mean that new value affected by event doesn't depend on the previous value.
 * The second mean that there are some (? commutative) operation on the domain it is performed on
 *
 * TODO: extract these two types (domains)
 * Maybe make events applicable only for selected domains? Not for the whole state
 */

private val logger = KotlinLogging.logger {}

// All of these actions currently imply that input is correct.
// TODO: add validation (as a separate class or method)

sealed class UserAction {
    abstract val actionId: Int // incrementing id. todo: make it autoincrement
    abstract var aroseAtTime: Millis

    /**
     * Apply action to both diff and globalState.
     *
     * globalState is needed for global state retrieval
     * diff.playerStates also contains player
     *
     * TODO: ensure playerState's and globalState's immutability
     */
    abstract fun getApplication(playerState: PlayerState, globalState: GameState): (diff: GameState) -> Unit
}

/**
 * Active action is something continuous that cannot exists simultaneously with any other
 * active action for the player. It can be: moving, chopping down the tree etc.
 *
 * onCancelOrFinish fires before switching to another active action and after player disconnects.
 */
sealed class ActiveUserAction : UserAction() {
    abstract fun onCancelOrFinish(playerState: PlayerState, globalState: GameState): (diff: GameState) -> Unit
}

class CancelActiveAction(
    override val actionId: Int, override var aroseAtTime: Millis,
    private val endPosition: Position
) : ActiveUserAction() {
    override fun apply(playerState: PlayerState, globalState: GameState) = { diffPlayerState: PlayerState, diff: GameState ->
        diffPlayerState.activeAction = VariableWithEmptyValue.empty()
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
