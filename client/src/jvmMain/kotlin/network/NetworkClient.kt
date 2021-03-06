package network

import com.whirvis.jraknet.RakNetPacket
import com.whirvis.jraknet.client.RakNetClient
import com.whirvis.jraknet.client.RakNetClientListener
import com.whirvis.jraknet.peer.RakNetServerPeer
import com.whirvis.jraknet.protocol.Reliability
import network.protocol.NetworkEvent
import network.protocol.NetworkEventManager
import kotlinx.coroutines.CompletableDeferred
import java.net.InetSocketAddress

class NetworkClient(private val eventListener: (NetworkEvent) -> Unit) : RakNetClientListener {
    private val client = RakNetClient()

    @PublishedApi
    internal var awaiters = mutableListOf<(NetworkEvent) -> Boolean>()

    fun connect(url: String, port: Int) {
        client.connect(url, port)
        client.addListener(this)
    }

//    fun sendUnreliably(event: NetworkEvent) {
//        todo: тут че?
//        client.sendMessage(Reliability.UNRELIABLE, event.getPreparedPacket())
//    }

    fun sendReliably(event: NetworkEvent) {
        client.sendMessage(Reliability.RELIABLE, event.getPreparedPacket())
    }

    suspend inline fun <reified R : NetworkEvent> sendReliablyAndAwait(
        event: NetworkEvent
    ): R {
        return sendReliablyAndAwait(event) { true }
    }

    suspend inline fun <reified R : NetworkEvent> sendReliablyAndAwait(
        event: NetworkEvent,
        crossinline responseAwaiter: (R) -> Boolean
    ): R {
        val deferred = registerAwaiter(responseAwaiter)
        sendReliably(event)
        return deferred.await()
    }

    inline fun <reified T : NetworkEvent> registerAwaiter(crossinline responseAwaiter: (T) -> Boolean): CompletableDeferred<T> {
        val deferred = CompletableDeferred<T>()
        awaiters.add {
            if (it is T && responseAwaiter(it)) {
                deferred.complete(it)
                return@add true
            }
            return@add false
        }
        return deferred
    }

    override fun onLogin(client: RakNetClient, peer: RakNetServerPeer) {
        println("logged in")
        peer.timeout = 1000_000_000_000L // for debug
    }

    override fun handleMessage(client: RakNetClient, peer: RakNetServerPeer, packet: RakNetPacket, channel: Int) {
        peer.timeout = 1000_000_000_000L // for debug
        val event = NetworkEventManager.resolveEvent(packet) ?: error(
            "Event with ${packet.id} is not registered. Maybe forgot to add it to initialization?"
        )

        awaiters.removeIf { it(event) }

        eventListener(event)
    }

    override fun handleUnknownMessage(client: RakNetClient, peer: RakNetServerPeer, packet: RakNetPacket, channel: Int) {
        println("handleUnknownMessage")
    }

    override fun onHandlerException(client: RakNetClient, address: InetSocketAddress, throwable: Throwable) {
        println("onHandlerException $throwable")
    }

    override fun onPeerException(client: RakNetClient, peer: RakNetServerPeer, throwable: Throwable) {
        println("onPeerException $throwable")
    }
}
