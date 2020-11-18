package state.entity

import state.action.ActiveUserAction
import state.SpatialState
import state.VariableWithEmptyValue
import state.Millis


/**
 * It is temporary class. It can be replaced with something more cool
 * (which will support multiple messages in text log, like in MMORPG's)
 */
class SpeechState(
    val message: String,
    val messageAroseAtTime: Millis
)

// everywhere in state, null means no difference and <> means cancelled
open class MobState(
    var spatialState: SpatialState?,
    var activeAction: VariableWithEmptyValue<ActiveUserAction>?,
    var speechState: SpeechState?,
) {
    open fun subtractDiff(rhs: PlayerState) {
        spatialState = if (spatialState == rhs.spatialState) null else spatialState
        check(
            spatialState == null || rhs.spatialState == null ||
                spatialState!!.movementStartedAtServerTime > rhs.spatialState!!.movementStartedAtServerTime
        )

        activeAction = if (activeAction == rhs.activeAction) null else activeAction

        speechState = if (speechState == rhs.speechState) null else speechState
    }
}
