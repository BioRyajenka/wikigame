package generation.generator

import generation.PropertyDesc
import generation.TypeDesc
import com.squareup.kotlinpoet.*
import kotlin.reflect.KType
import kotlin.reflect.full.createType

fun generateNetworkTransferFunctions(typeDef: TypeDesc): List<FunSpec> {

    val readFuncSpec = generateReadFunction(typeDef)
    val writeFuncSpec = generateWriteFunction(typeDef)

    return listOf(readFuncSpec, writeFuncSpec)
}

private fun typeToShortName(type: KType): String? {
    return when (typeToClass(type)) {
        Integer::class.java -> "Int"
        Int::class.java -> "Int"
        String::class.java -> "String"
        Double::class.java -> "Double"
        Float::class.java -> "Float"
        else -> null
    }
}

private fun generateReadFunction(typeDef: TypeDesc): FunSpec {
    val rakNetPacketClassName = ClassName("com.whirvis.jraknet", "RakNetPacket")

    return FunSpec.builder("read${typeDef.clazz.simpleName}")
        .addModifiers(KModifier.INTERNAL)
        .addParameter("packet", rakNetPacketClassName)
        .returns(typeDef.clazz)
        .also { builder ->
            if (typeDef.parent != null) {
                builder.addStatement("val parent = read${typeDef.parent!!.clazz.simpleName}(packet)")
            }

            typeDef.fields.forEach { field ->
                genReadForProperty("val ${field.name} = ", field.type, builder)
            }
            val constructorArgs = typeDef.constructorArguments.joinToString { arg ->
                if (typeDef.fields.any { it.name == arg.name }) arg.name else "parent.${arg.name}"
            }
            builder.addStatement("return ${typeDef.clazz.simpleName}($constructorArgs)")
        }.build()
}

private fun genReadForProperty(prefix: String, type: KType, builder: FunSpec.Builder) {
    val typeShortName = typeToShortName(type)

    when {
        type.isMarkedNullable -> {
            // true == not null
            builder.addStatement("${prefix}if (packet.readBoolean()) {")
            builder.addCode(CodeBlock.Builder().indent().build())
            genReadForProperty("", type.classifier!!.createType(nullable = false, arguments = type.arguments), builder)
            builder.addCode(CodeBlock.Builder().unindent().build())
            builder.addStatement("} else null")
        }
        typeToClass(type) == List::class.java -> {
            builder.beginControlFlow("$prefix(0 until packet.readInt()).map")
            genReadForProperty("", type.arguments[0].type!!, builder)
            builder.endControlFlow()
        }
        typeToClass(type) == Map::class.java -> {
            builder.addStatement("$prefix(0 until packet.readInt()).associate {")
            builder.addCode(CodeBlock.Builder().indent().build())
            genReadForProperty("val k = ", type.arguments[0].type!!, builder)
            genReadForProperty("val v = ", type.arguments[1].type!!, builder)
            builder.addStatement("k to v")
            builder.addCode(CodeBlock.Builder().unindent().build())
            builder.addStatement("}.toMutableMap()")
        }
        typeShortName != null -> builder.addStatement("${prefix}packet.read$typeShortName()")
        type.arguments.isNotEmpty() -> builder.addStatement("${prefix}read${type.name}<${type.arguments.joinToString()}>(packet)")
        else -> builder.addStatement("${prefix}read${type.name}(packet)")
    }
}

private fun generateWriteFunction(typeDef: TypeDesc): FunSpec {
    val rakNetPacketClassName = ClassName("com.whirvis.jraknet", "RakNetPacket")
    val obj = "obj"

    return FunSpec.builder("write${typeDef.clazz.simpleName}")
        .addModifiers(KModifier.INTERNAL)
        .addParameter(obj, typeDef.clazz)
        .addParameter("packet", rakNetPacketClassName)
        .also { builder ->
            if (typeDef.parent != null) {
                builder.addStatement("write${typeDef.parent!!.clazz.simpleName}($obj, packet)")
            }

            typeDef.fields.forEach { field ->
                genWriteForProperty(obj, field, builder)
            }
        }.build()
}

private fun genWriteForProperty(obj: String?, field: PropertyDesc, builder: FunSpec.Builder) {
    val typeShortName = typeToShortName(field.type)
    val variable = if (obj == null) field.name else "$obj.${field.name}"

    when {
        field.type.isMarkedNullable -> {
            // true == not null
            builder.addStatement("packet.writeBoolean($variable != null)")
            builder.beginControlFlow("if ($variable != null)")
            val nnProp = PropertyDesc(field.type.classifier!!.createType(nullable = false, arguments = field.type.arguments), field.name)
            genWriteForProperty(obj, nnProp, builder)
            builder.endControlFlow()
        }
        typeToClass(field.type) == List::class.java -> {
            builder.addStatement("packet.writeInt($variable.size)")
            builder.beginControlFlow("$variable.forEach")
            val itProp = PropertyDesc(field.type.arguments[0].type!!, "it")
            genWriteForProperty(null, itProp, builder)
            builder.endControlFlow()
        }
        typeToClass(field.type) == Map::class.java -> {
            builder.addStatement("packet.writeInt($variable.size)")
            builder.addStatement("$variable.forEach { (k, v) ->")
            val kProp = PropertyDesc(field.type.arguments[0].type!!, "k")
            val vProp = PropertyDesc(field.type.arguments[1].type!!, "v")
            builder.addCode(CodeBlock.Builder().indent().build())
            genWriteForProperty(null, kProp, builder)
            genWriteForProperty(null, vProp, builder)
            builder.addCode(CodeBlock.Builder().unindent().build())
            builder.addStatement("}")
        }
        typeShortName == "Float" -> builder.addStatement("packet.writeFloat($variable.toDouble())")
        typeShortName != null -> builder.addStatement("packet.write$typeShortName($variable)")
        else -> builder.addStatement("write${field.type.name}($variable!!, packet)")
    }
}
