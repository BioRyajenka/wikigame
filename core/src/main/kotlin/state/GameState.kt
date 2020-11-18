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
    // fun applyDelta(gameStateDelta: GameStateDelta) TODO: this method refers to client package

    // TODO: add tests
    fun subtractDiff(rhs: GameState) {
//        playerState.subtractDiff(rhs.playerState)
//         TODO: other things like:
//        otherPlayerStates...
//        groundEntities
    }
}

typealias GameStateDiff = GameState // TODO: use it? maybe separate diff & not-diff classes?
