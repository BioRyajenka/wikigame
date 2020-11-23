package generation.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import generation.*

fun isDiffFieldNullable(field: PropertyDesc): Boolean {
//    return field.name != "id" && typeToClass(field.type) != java.util.Map::class.java
//    return field.type.isMarkedNullable
    return isDiffFieldNullable(field.type.asTypeName(), field.name)
}

fun isDiffFieldNullable(type: TypeName, varName: String?): Boolean {
    return varName != "id" && (type !is ParameterizedTypeName || type.rawType.simpleName != "Map")
}

private fun remapFieldType(field: PropertyDesc): TypeName {
    return remapType(field.type.asTypeName(), field.name)
}

private fun remapType(type: TypeName, varName: String?): TypeName {
    val nullable = isDiffFieldNullable(type, varName)

    if (type is ParameterizedTypeName) {
        val typeArguments = type.typeArguments.map {
            check(!it.isNullable)
            remapType(it, null).copy(nullable = false)
        }
        if (type.rawType.simpleName == "Map") {
            check(!nullable) { "Most probably Map shouldn't be nullable" }
            check(typeArguments.size == 2)

            return MUTABLE_MAP.parameterizedBy(listOf(
                typeArguments[0],
                typeArguments[1].copy(nullable = true)
            ))
        }
        return type.rawType.parameterizedBy(typeArguments).copy(nullable = nullable)
    }

    return if ((type as ClassName).simpleName.endsWith(STATE_DEF_SUFFIX)) {
        diffClassName(type.simpleName).copy(nullable = nullable)
    } else type.copy(nullable = nullable, annotations = emptyList())
}

private fun diffClassName(className: String): ClassName {
    return ClassName(RESULT_PACKAGE_NAME, className.removeSuffix(STATE_DEF_SUFFIX) + "StateDiff")
}

fun diffClassName(clazz: Class<*>): ClassName {
    return diffClassName(clazz.simpleName)
}

fun generateDiffClass(typeDef: TypeDesc): TypeSpec {
    val classNameObj = diffClassName(typeDef.clazz)

    return TypeSpec.classBuilder(classNameObj)
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
                    PropertySpec.builder(
                        field.name,
                        remapFieldType(field)
                    )
                        .mutable(true)
                        .initializer(field.name)
                        .build()
                builder.addProperty(propertySpec)
            }

            if (typeDef.parent != null) {
                builder.superclass(diffClassName(typeDef.clazz.superclass))
                val superConstructorArgs = typeDef.parent!!.constructorArguments
                builder.addSuperclassConstructorParameter(superConstructorArgs.joinToString { it.name })
            }

            addMinusFunction(typeDef, builder)
            addToCompleteStateFunction(typeDef, builder)
        }.build()
}

private fun addToCompleteStateFunction(typeDef: TypeDesc, tBuilder: TypeSpec.Builder) {
    val functionName = "toCompleteState"
    val stateClassName = stateClassName(typeDef.clazz)
    val modifier = when {
        typeDef.parent != null -> KModifier.OVERRIDE
        typeDef.isOpen -> KModifier.OPEN
        else -> KModifier.FINAL
    }

    tBuilder.addFunction(
        FunSpec.builder(functionName)
            .addModifiers(modifier)
            .returns(stateClassName)
            .also { builder ->
                typeDef.constructorArguments.forEach { arg ->
                    val prefix = "val ${arg.name} = ${arg.name}" + if (isDiffFieldNullable(arg)) "!!" else ""

                    when {
                        typeToClass(arg.type) == List::class.java
                            && arg.type.arguments[0].type!!.name.endsWith(STATE_DEF_SUFFIX) -> {
                            builder.addStatement("$prefix.map { it.toCompleteState() }")
                        }
                        typeToClass(arg.type) == Map::class.java -> {
                            if (arg.type.arguments[1].type!!.name.endsWith(STATE_DEF_SUFFIX)) {
                                builder.addStatement("$prefix.mapValues { it.value!!.toCompleteState() }.toMutableMap()")
                            } else {
                                builder.addStatement("$prefix.mapValues { it.value!! }.toMutableMap()")
                            }
                        }
                        arg.type.name.endsWith(STATE_DEF_SUFFIX) -> {
                            builder.addStatement("$prefix.toCompleteState()")
                        }
                        else -> builder.addStatement(prefix)
                    }
                }

                val args = typeDef.constructorArguments.joinToString { it.name }
                builder.addStatement("return ${stateClassName.simpleName}($args)")
            }
            .build()
    )
}

private fun addMinusFunction(typeDef: TypeDesc, tBuilder: TypeSpec.Builder) {
    val rhs = "prevDiff"
    val functionName = "minusAssign"

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

                    if (isDiffFieldNullable(field)) {
                        builder.beginControlFlow("if (${field.name} == null)")
                            .addStatement("check($rhs.${field.name} == null)")
                            .addCode(CodeBlock.Builder().unindent().build())
                            .addStatement("} else {")
                            .addCode(CodeBlock.Builder().indent().build())
                            .also {
                                if (field.type.name.endsWith(STATE_DEF_SUFFIX)) {
                                    builder.addStatement("if ($rhs.${field.name} != null) ${field.name}!!.$functionName($rhs.${field.name}!!)")
                                } else {
                                    builder.addStatement("if (${field.name} == $rhs.${field.name}) ${field.name} = null")
                                }
                            }
                            .endControlFlow()
                    } else {
                        check(typeToClass(field.type) == Map::class.java || field.name == "id")
                        if (typeToClass(field.type) == Map::class.java) {
                            builder.addCode("""
                                ${field.name}.forEach { (k, v) ->
                                    if (v == $rhs.${field.name}[k]) ${field.name}.remove(k)
                                }
                            """.trimIndent()
                            )
                            builder.addStatement("")
                        }
                    }
                }
            }
            .build()
    )
}
