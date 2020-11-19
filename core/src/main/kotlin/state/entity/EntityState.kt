package state.entity

import state.Dimension
import state.Vector

// entity is what we can see on the map
open class EntityState(open val id: String) : Vector() {
    // TODO: move spatial state here
}

/*class ItemsStackEntity(
    override val id: String,
    override var x: Dimension,
    override var y: Dimension,
    val itemId: String,
    val amount: Int
) : EntityState()*/
