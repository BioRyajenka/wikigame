package state

import state.Millis
import state.Position
import state.Speed


class SpatialState(
    val startPosition: Position,
    val endPosition: Position,
    val speed: Speed,
    val movementStartedAtServerTime: Millis
)
