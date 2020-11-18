import state.Millis

object TimeProvider {
    val currentTime: Millis
        get() = System.currentTimeMillis().toDouble()
}
