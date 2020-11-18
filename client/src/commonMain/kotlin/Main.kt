import com.soywiz.korio.async.async
import network.NetworkEntry
import ui.UIEntry
import kotlin.coroutines.coroutineContext


suspend fun main() {
    async(coroutineContext) { NetworkEntry() }
//    UIEntry()
}
