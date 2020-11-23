package state.entity

import generation.StateDef
import generation.TransferableViaNetwork
import state.Position
import state.action.ActiveUserAction
import state.VariableWithEmptyValue
import state.Speed

@TransferableViaNetwork
data class PersonalInfo(val movementSpeed: Speed)//, val choppingTreesSpeed: Speed)

@TransferableViaNetwork
data class User(val id: String, val name: String)

@StateDef
class PlayerStateDef(
    id: String,
    position: Position,
    activeAction: VariableWithEmptyValue<ActiveUserAction>,
    speechState: VariableWithEmptyValue<SpeechState>,

//    val inventory: InventoryState,
//    val tradeState: TradeState,
//    var tradeWithPlayerId: VariableWithEmptyValue<String>?,

    val personalInfo: PersonalInfo,
    val publicInfo: User,
) : MobStateDef(
    id,
    position,
    activeAction,
    speechState,
)
