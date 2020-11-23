package generation.generator

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KType
import kotlin.reflect.javaType

internal val KType.name: String
    get() = typeToClass(this).simpleName

@OptIn(ExperimentalStdlibApi::class)
internal fun typeToClass(type: KType): Class<*> {
    val javaType = type.javaType
    return if (javaType is ParameterizedType) {
        javaType.rawType as Class<*>
    } else {
        javaType as Class<*>
    }
}
