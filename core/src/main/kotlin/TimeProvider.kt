package core

import core.world.Millis

interface TimeProvider {
    val currentTime: Millis
}
