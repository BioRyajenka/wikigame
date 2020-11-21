package state.entity

import state.Position
import state.action.ActiveUserAction
import state.VariableWithEmptyValue
import state.Speed

data class PersonalInfo(val movementSpeed: Speed)//, val choppingTreesSpeed: Speed)
data class User(val id: String, val name: String)

class PlayerState(
    id: String,
    position: Position?,
    activeAction: VariableWithEmptyValue<ActiveUserAction>?,
    speechState: VariableWithEmptyValue<SpeechState>?,

//    val inventory: InventoryState,
//    val tradeState: TradeState,
//    var tradeWithPlayerId: VariableWithEmptyValue<String>?,

    val personalInfo: PersonalInfo,
    val publicInfo: User,
) : MobState(
    id,
    position,
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
