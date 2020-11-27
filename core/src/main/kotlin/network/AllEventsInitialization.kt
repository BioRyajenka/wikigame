package network

import network.protocol.GameStateDiffEvent
import network.protocol.JoinWorldRequest
import network.protocol.JoinWorldResponse

fun initializeNetworkEvents() {
    JoinWorldRequest
    JoinWorldResponse
    GameStateDiffEvent
}

/*
doesn't work:

fun foo() {
    // api("org.reflections:reflections:0.9.12")
    val packagePath = "core.network.protocol"
    val reflections =
        Reflections(
            ConfigurationBuilder()
                .filterInputsBy(FilterBuilder().includePackage(packagePath))
                .setUrls(ClasspathHelper.forPackage(packagePath))
                .setScanners(SubTypesScanner(false))
        )
    val typeList = reflections.getSubTypesOf(Object::class.java)
    typeList.forEach { c ->
        println(c.kotlin)
    }
}*/
