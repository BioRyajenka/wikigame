package generation.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import generation.RESULT_PACKAGE_NAME
import generation.STATE_DEF_SUFFIX
import generation.TypeDesc

private fun remapType(type: TypeName): TypeName {
    check(!type.isNullable) { "State props can't be nullable" }
    if (type is ParameterizedTypeName) {
        val typeArguments = type.typeArguments.map {
            check(!it.isNullable)
            remapType(it)
        }
        if (type.rawType.simpleName == "Map") {
            return MAP.parameterizedBy(typeArguments)
        }
        return type.rawType.parameterizedBy(typeArguments)
    }

    return if ((type as ClassName).simpleName.endsWith(STATE_DEF_SUFFIX)) {
        immutableClassName(type.simpleName)
    } else type.copy(annotations = emptyList())
}

private fun immutableClassName(className: String): ClassName {
    return ClassName(RESULT_PACKAGE_NAME, "Immutable" + className.removeSuffix(STATE_DEF_SUFFIX) + "State")
}

fun immutableClassName(clazz: Class<*>): ClassName {
    return immutableClassName(clazz.simpleName)
}

fun generateImmutableClass(typeDef: TypeDesc): TypeSpec {
    val classNameObj = immutableClassName(typeDef.clazz)

    return TypeSpec.interfaceBuilder(classNameObj)
        .also { builder ->
            typeDef.fields.forEach { field ->
                val typeClazz = typeToClass(field.type)
                check (typeClazz.isPrimitive || typeClazz.`package` != null) {
                    "Parameter types without package (or somehow bad package?) are not supported due to the bug in the kotlinpoet"
                }
                builder.addProperty(field.name, remapType(field.type.asTypeName()))
            }
            if (typeDef.parent != null) {
                builder.addSuperinterface(immutableClassName(typeDef.parent!!.clazz))
            }
        }.build()
}
