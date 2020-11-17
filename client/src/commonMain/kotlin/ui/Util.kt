package ui

import kotlin.math.sqrt

/**
 * @author Igor Sushencev
 * @since 28.01.19
 * Copyright (c) Huawei Technologies Co., Ltd. 2015-2019. All rights reserved.
 */

typealias Dimension = Float

fun Number.toDimension() = this.toFloat()

typealias Millis = Double

/**
 * One dimension per milli
 */
typealias Speed = Float

typealias Position = Vector

open class Vector {
    var x: Dimension
    var y: Dimension

    constructor(x: Number, y: Number) {
        this.x = x.toDimension()
        this.y = y.toDimension()
    }

    constructor() : this(0, 0)

    val length: Dimension
        get() = sqrt(x * x + y * y)

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

    override fun toString(): String {
        return "($x, $y)"
    }

    fun copy(): Vector {
        return Vector(x, y)
    }
}

data class IntPosition(val i: Int, val j: Int)

data class IntSize(val width: Int, val height: Int)
