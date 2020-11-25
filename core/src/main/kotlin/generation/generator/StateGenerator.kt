package generation.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import generation.*

private fun remapFieldType(field: PropertyDesc): TypeName {
    return remapType(field.type.asTypeName())
}

private fun remapType(type: TypeName): TypeName {
    check(!type.isNullable) { "State props can't be nullable" }
    if (type is ParameterizedTypeName) {
        val typeArguments = type.typeArguments.map {
            check(!it.isNullable)
            remapType(it)
        }
        if (type.rawType.simpleName == "Map") {
            return MUTABLE_MAP.parameterizedBy(typeArguments)
        }
        return type.rawType.parameterizedBy(typeArguments)
    }

    return if ((type as ClassName).simpleName.endsWith(STATE_DEF_SUFFIX)) {
        stateClassName(type.simpleName)
    } else type.copy(annotations = emptyList())
}

private fun stateClassName(className: String): ClassName {
    return ClassName(RESULT_PACKAGE_NAME, className.removeSuffix(STATE_DEF_SUFFIX) + "State")
}

fun stateClassName(clazz: Class<*>): ClassName {
    return stateClassName(clazz.simpleName)
}

fun generateStateClass(typeDef: TypeDesc): TypeSpec {
    val classNameObj = stateClassName(typeDef.clazz)
    val immutableClassObj = immutableClassName(typeDef.clazz)

    return TypeSpec.classBuilder(classNameObj)
        .addSuperinterface(immutableClassObj)
        .primaryConstructor(
            FunSpec.constructorBuilder().also { builder ->
                typeDef.constructorArguments.forEach { field ->
                    builder.addParameter(field.name, remapFieldType(field))
                }
            }.build()
        )
        .addAnnotation(TransferableViaNetwork::class.java)
        .also { builder ->
            if (typeDef.isOpen) {
                builder.addModifiers(KModifier.OPEN)
            }

            typeDef.fields.forEach { field ->
                val propertySpec =
                    PropertySpec.builder(field.name, remapFieldType(field), KModifier.OVERRIDE)
                        .initializer(field.name)
                        .mutable(true)
                        .build()
                builder.addProperty(propertySpec)
            }

            if (typeDef.parent != null) {
                builder.superclass(stateClassName(typeDef.parent!!.clazz))
                val superConstructorArgs = typeDef.parent!!.constructorArguments
                builder.addSuperclassConstructorParameter(superConstructorArgs.joinToString { it.name })
            }

            addPlusFunction(typeDef, builder)
        }.build()
}

private fun addPlusFunction(typeDef: TypeDesc, tBuilder: TypeSpec.Builder) {
    val rhs = "diff"
    val functionName = "plusAssign"

    tBuilder.addFunction(
        FunSpec.builder(functionName)
            .addModifiers(KModifier.OPERATOR)
            .addParameter(rhs, diffClassName(typeDef.clazz))
            .also { builder ->
                if (typeDef.parent != null) {
                    builder.addStatement("super.$functionName($rhs)")
                }

                typeDef.fields.forEach { field ->
                    if (typeDef.parent != null) builder.addStatement("")

                    // не nullable - это map или id
                    if (isDiffFieldNullable(field)) {
                        builder.beginControlFlow("if ($rhs.${field.name} != null)")

                        if (field.type.name.endsWith(STATE_DEF_SUFFIX)) {
                            builder.addStatement("${field.name}.$functionName($rhs.${field.name}!!)")
                        } else {
                            builder.addStatement("${field.name} = $rhs.${field.name}!!")
                        }
                        builder.endControlFlow()
                    } else {
                        check(typeToClass(field.type) == Map::class.java || field.name == "id")

                        if (typeToClass(field.type) == Map::class.java) {
                            if (field.type.arguments[1].type!!.name.endsWith(STATE_DEF_SUFFIX)) {
                                builder.addCode("""
                                $rhs.${field.name}.forEach { (k, v) ->
                                    when {
                                        v == null -> ${field.name}.remove(k)
                                        k in ${field.name} -> ${field.name}[k]!!.plusAssign(v)
                                        else -> ${field.name}[k] = v.toCompleteState()
                                    }
                                }
                                """.trimIndent())
                            } else {
                                builder.addCode("""
                                $rhs.${field.name}.forEach { (k, v) ->
                                    if (v == null) ${field.name}.remove(k)
                                    else ${field.name}[k] = v
                                }
                                """.trimIndent())
                            }
                            builder.addStatement("")
                        }
                    }
                }
            }
            .build()
    )
}

