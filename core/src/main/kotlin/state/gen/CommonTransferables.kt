package state.gen

import com.whirvis.jraknet.RakNetPacket
import state.VariableWithEmptyValue
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

private fun allTransferFunctionsInitializer(): Map<String, KFunction<*>> {
    val selfRef = ::allTransferFunctionsInitializer
    val currentClass = selfRef.javaMethod!!.declaringClass
    val classDefiningFunctions = currentClass.classLoader.loadClass("state.gen.TransferableKt")
    return classDefiningFunctions.methods.filter { Modifier.isStatic(it.modifiers) }.associate { method ->
        method.name to method!!.kotlinFunction!!
    }
}

private val allTransferFunctions = allTransferFunctionsInitializer()

internal fun writeArbitrary(obj: Any, packet: RakNetPacket) {
    val className = obj::class.simpleName!!
    packet.writeString(className)
    allTransferFunctions.getValue("write$className").call(obj, packet)
}

internal fun <T> readArbitrary(packet: RakNetPacket, className: String? = null): T {
    val realClassName = className ?: packet.readString()

    // TODO: can improve performance by removing casts
    //  (by turning generated code into Action's (or any Transferable) method)
    //  so, Transferable should be interface?
    @Suppress("UNCHECKED_CAST")
    return allTransferFunctions.getValue("read$realClassName").call(packet) as T
}

internal fun <T> writeVariableWithEmptyValue(obj: VariableWithEmptyValue<T>, packet: RakNetPacket) {
    if (obj.empty()) {
        packet.writeString("null")
    } else {
        writeArbitrary(obj.getValue()!!, packet)
    }
}

internal fun <T> readVariableWithEmptyValue(packet: RakNetPacket): VariableWithEmptyValue<T> {
    val className = packet.readString()
    return if (className == "null") {
        VariableWithEmptyValue.empty()
    } else {
        VariableWithEmptyValue.ofValue(readArbitrary(packet, className))
    }
}
