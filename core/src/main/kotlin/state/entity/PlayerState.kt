package core.state.entity

import core.state.action.ActiveUserAction
import core.state.SpatialState
import core.state.VariableWithEmptyValue
import core.world.Speed

data class PersonalInfo(val movementSpeed: Speed, val choppingTreesSpeed: Speed)
data class User(val id: String, val name: String)

class PlayerState(
    spatialState: SpatialState?,
    activeAction: VariableWithEmptyValue<ActiveUserAction>?,
    speechState: SpeechState?,

//    val inventory: InventoryState,
//    val tradeState: TradeState,
//    var tradeWithPlayerId: VariableWithEmptyValue<String>?,

    val personalInfo: PersonalInfo,
    val publicInfo: User,
) : MobState(
    spatialState,
    activeAction,
    speechState,
) {
    override fun subtractDiff(rhs: PlayerState) {
//        if (tradeWithPlayerId == rhs.tradeWithPlayerId) {
//            tradeState.subtractDiff(rhs.tradeState);
//        } // else just accept our version
//
//        inventory.subtractDiff(rhs.inventory)
//
//        super.subtractDiff(rhs)
    }
}
