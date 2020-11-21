package state


class MapState(
    val field: Map<IntPosition, MapCell>
)

class MapCell(
    val tileTypeByLayer: List<Int>,
)
