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

fun Number.toDimension(): Dimension = this.toFloat()

typealias Millis = Double

val Int.hz: Millis
    get() = 1000.0 / this

/**
 * One dimension per milli
 */
typealias Speed = Float

typealias Position = Vector

@TransferableViaNetwork
open class Vector(open var x: Dimension, open var y: Dimension) {
    constructor() : this(0f, 0f)
    constructor(x: Number, y: Number) : this(x.toDimension(), y.toDimension())

    operator fun minus(rhs: Vector) = Vector(x - rhs.x, y - rhs.y)

    operator fun plus(rhs: Vector) = Vector(x + rhs.x, y + rhs.y)

    operator fun div(rhs: Number) = Vector(x / rhs.toDimension(), y / rhs.toDimension())

    operator fun times(rhs: Number) = Vector(x * rhs.toDimension(), y * rhs.toDimension())

    operator fun minusAssign(rhs: Vector) {
        x -= rhs.x
        y -= rhs.y
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    fun copy(modifier: (Vector.() -> Unit)? = null): Vector {
        val copy = Vector(x, y)
        if (modifier != null) copy.modifier()
        return copy
    }

    val vectorLength: Float
        get() = sqrt(x * x + y * y)
}

@TransferableViaNetwork
data class IntPosition(val i: Int, val j: Int)

data class IntSize(val width: Int, val height: Int)
