package core.state

// item is what is in the inventory
class Item(val id: String)


/*
// todo: maybe not private?
private typealias ItemId = String
// amount can be negative
private typealias ItemAmount = Int

private fun subtractItems(from: MutableMap<ItemId, ItemAmount>, what: MutableMap<ItemId, ItemAmount>) {
    (from.keys + what.keys).forEach { key ->
        val newAmount = from.getOrDefault(key, 0) - what.getOrDefault(key, 0)
        if (newAmount != 0) {
            from[key] = newAmount
        } else if (key in from) {
            from -= key
        }
    }
}

data class TradeState(
    var state: TradeStateAcceptanceState?,
    var tradeStartedAt: Millis?,
    val ourOffer: MutableMap<ItemId, ItemAmount>,
    val hisOffer: MutableMap<ItemId, ItemAmount>
) {
    enum class TradeStateAcceptanceState {
        REQUESTED, RECEIVED, HAPPENS, CANCELLED, FINISHED
    }

    fun subtractDiff(rhs: TradeState) {
        subtractItems(ourOffer, rhs.ourOffer)
        subtractItems(hisOffer, rhs.hisOffer)
        state = if (state == rhs.state) null else state
        tradeStartedAt = if (tradeStartedAt == rhs.tradeStartedAt) null else tradeStartedAt
    }
}

data class InventoryState(
    val items: MutableMap<ItemId, ItemAmount>
) {
    fun subtractDiff(rhs: InventoryState) {
        subtractItems(items, rhs.items)
    }
}
*/
