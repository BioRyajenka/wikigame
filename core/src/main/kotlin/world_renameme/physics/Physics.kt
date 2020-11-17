package core.world.physics

import core.world.Dimension
import core.world.Millis
import core.world.Vector

/**
 * @author Igor Sushencev
 * @since 28.01.19
 * Copyright (c) Huawei Technologies Co., Ltd. 2015-2019. All rights reserved.
 */

/*class SpeedVector(
    private val dxPerMillisecond: Dimension,
    private val dyPerMillisecond: Dimension
) {
    fun getPassedDistanceByElapsedTime(deltaTime: Millis): Vector {
        return Vector(
            (dxPerMillisecond * deltaTime).toFloat(),
            (dyPerMillisecond * deltaTime).toFloat()
        )
    }
}*/

//open class PhysicalModel(
//    x: DimensionType,
//    y: DimensionType,
//    val speed: SpeedVector,
//    val moveDirection: Vector
//) : Position(x, y) {
//    constructor(): this(0f, 0f,
//        SpeedVector(0f, 0f),
//        Vector(0f, 0f)
//    )
//}
