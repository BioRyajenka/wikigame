package state

import state.entity.EntityState
import mu.KotlinLogging

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
data class GameState(
    val entities: Map<String, EntityState>, // id is a key
    val mapState: MapState,
//    var time: Millis // the time this state describes
) {
    //  TODO: this method should be in client's module
    /**
     * Please note that our + is not commutative
     */
//    operator fun plusAssign(diff: GameStateDiff) {
//
//    }

    //  TODO: this method should be in server's module
    /**
     * Subtracts diff from this
     *
     * if there are a+b, then (a+b)-a should be b
     * + is apply, - is subtract
     */
    operator fun minusAssign(diff: GameStateDiff) {

    }
}

typealias GameStateDiff = GameState // TODO: use it? maybe separate diff & not-diff classes?
//class GameStateDiff(val gameState: GameState, val fromVersion: Int, val toVersion: Int, val lastUserActionId: Int)
