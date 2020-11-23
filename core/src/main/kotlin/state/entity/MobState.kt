package state.entity

import generation.StateDef
import generation.TransferableViaNetwork
import state.action.ActiveUserAction
import state.VariableWithEmptyValue
import state.Millis
import state.Position


/**
 * It is temporary class. It can be replaced with something more cool
 * (which will support multiple messages in text log, like in MMORPG's)
 */
@TransferableViaNetwork
class SpeechState(
    val message: String,
    val messageAroseAtTime: Millis
)

// everywhere in state, null means no difference and <> means cancelled
@StateDef
open class MobStateDef(
    id: String,
    position: Position,
    var activeAction: VariableWithEmptyValue<ActiveUserAction>,
    var speechState: VariableWithEmptyValue<SpeechState>,
) : EntityStateDef(id, position)
