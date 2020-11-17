package core.state.entity

import core.world.Dimension
import core.world.Vector

// entity is what we can see on the map
abstract class EntityState : Vector() {
    abstract val id: String
    abstract override var x: Dimension
    abstract override var y: Dimension
}

class ItemsStackEntity(
    override val id: String,
    override var x: Dimension,
    override var y: Dimension,
    val itemId: String,
    val amount: Int
) : EntityState()
