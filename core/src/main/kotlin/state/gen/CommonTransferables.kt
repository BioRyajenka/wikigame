package state.gen

import com.whirvis.jraknet.RakNetPacket
import state.VariableWithEmptyValue
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

private fun allInnerFunctionsInitializer(): Map<String, KFunction<*>> {
    val selfRef = ::allInnerFunctionsInitializer
    val currentClass = selfRef.javaMethod!!.declaringClass
    val classDefiningFunctions = currentClass.classLoader.loadClass("TransferableKt")
    return classDefiningFunctions.methods.filter { Modifier.isStatic(it.modifiers) }.associate { method ->
        method.name to method!!.kotlinFunction!!
    }
}

private val allInnerFunctions = allInnerFunctionsInitializer()

internal fun writeVariableWithEmptyValue(obj: Any, packet: RakNetPacket) {
    val className = obj::class.simpleName!!
    packet.writeString(className)
    allInnerFunctions.getValue("write$className").call(obj, packet)
}

internal fun <T> readVariableWithEmptyValue(packet: RakNetPacket): VariableWithEmptyValue<T> {
    val className = packet.readString()
    @Suppress("UNCHECKED_CAST")
    val obj = allInnerFunctions.getValue("read$className").call(packet) as T
    return VariableWithEmptyValue.ofValue(obj)
}
