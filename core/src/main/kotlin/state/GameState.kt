package state

import generation.StateDef
import mu.KotlinLogging
import state.entity.EntityStateDef
import state.entity.MobStateDef
import state.entity.PlayerStateDef

private val logger = KotlinLogging.logger {}

// this is a whole state for one player
// it should reflect every user and server event
// for server it is for networking purposes only
// for client it is main state
//
// server is also calculating average time on client and sends timestamps (like in VisibleProcess) according to that time
// time calculation is performed every, say, 20 seconds, using leaky algorithm:
// gameTime = (oldGameTime * 0.95) + ((serverTimeStamp + (rtt/2)) * 0.05) or something like this
//
// todo: server can use haffman compression to send deltas?
// todo: make immutable version for each mutable state (e.g. interface ImmutableGameState {val mobStates...}
//       maybe autogenerate
@StateDef
data class GameStateDef(
    val entities: Map<String, EntityStateDef>, // id is a key
    val mobs: Map<String, MobStateDef>,
    val players: Map<String, PlayerStateDef>,
    val mapState: MapStateDef,
//    var time: Millis // the time this state describes
)
