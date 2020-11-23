package state.entity

import generation.StateDef
import state.Dimension
import state.Position
import state.Vector

// entity is what we can see on the map
@StateDef
open class EntityStateDef(val id: String, val position: Position)

/*class ItemsStackEntity(
    override val id: String,
    override var x: Dimension,
    override var y: Dimension,
    val itemId: String,
    val amount: Int
) : EntityState()*/
