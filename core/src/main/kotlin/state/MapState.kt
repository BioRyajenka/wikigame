package state

import generation.StateDef
import generation.TransferableViaNetwork

@StateDef
class MapStateDef(
    val field: Map<IntPosition, MapCell>
)

@TransferableViaNetwork
class MapCell(
    val tileTypeByLayer: List<Int>,
)
