package core.state

import core.world.Millis
import core.world.Position
import core.world.Speed


class SpatialState(
    val startPosition: Position,
    val endPosition: Position,
    val speed: Speed,
    val movementStartedAtServerTime: Millis
)
