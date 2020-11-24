package state

import generation.TransferableViaNetwork
import kotlin.math.sqrt

//@TransferableViaNetwork
/**
 * in diff:
if (diff.speechState != null) {
    val value = speechState.getValue()
    val diffValue = diff.speechState!!.getValue()

    when {
        diffValue == null -> speechState = VariableWithEmptyValue.empty()
        value == null -> speechState = VariableWithEmptyValue.ofValue(diffValue)
        else -> value.plusAssign(diffValue)
    }
}
 */

class VariableWithEmptyValue<out T> private constructor(private var value: T? = null) {
    // null means empty value

    fun getValue(): T? = value

    fun empty() = value == null

    companion object {
        private val EMPTY = VariableWithEmptyValue<Any>(null)

        @Suppress("UNCHECKED_CAST")
        fun <T> empty() = EMPTY as VariableWithEmptyValue<T>
        fun <T> ofValue(value: T) = VariableWithEmptyValue(value)
    }
}

typealias Dimension = Float

typealias Millis = Double

typealias Hertz = Double

/**
 * One dimension per milli
 */
typealias Speed = Float

typealias Position = Vector

@TransferableViaNetwork
open class Vector(open var x: Dimension, open var y: Dimension) {
    constructor() : this(0f, 0f)

    operator fun minus(rhs: Vector) = Vector(x - rhs.x, y - rhs.y)

    operator fun plus(rhs: Vector) = Vector(x + rhs.x, y + rhs.y)

    operator fun div(rhs: Double) = Vector(x / rhs.toFloat(), y / rhs.toFloat())

    operator fun div(rhs: Float) = Vector(x / rhs, y / rhs)

    operator fun div(rhs: Int) = Vector(x / rhs, y / rhs)

    operator fun times(rhs: Double) = Vector(x * rhs.toFloat(), y * rhs.toFloat())

    operator fun times(rhs: Float) = Vector(x * rhs, y * rhs)

    operator fun times(rhs: Int) = Vector(x * rhs, y * rhs)

    operator fun minusAssign(rhs: Vector) {
        x -= rhs.x
        y -= rhs.y
    }

    fun vectorLength(): Float = sqrt(x * x + y * y)
}

@TransferableViaNetwork
data class IntPosition(val i: Int, val j: Int)

data class IntSize(val width: Int, val height: Int)
