package ui

import com.soywiz.korge.view.View
import state.Vector
import state.toDimension

operator fun Vector.minus(view: View): Vector {
    return copy { x -= view.x.toDimension(); y -= view.y.toDimension(); }
}
