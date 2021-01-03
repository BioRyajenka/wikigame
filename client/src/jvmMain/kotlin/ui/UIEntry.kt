package ui

import GameStateHolder.gameState
import com.soywiz.korge.Korge
import state.action.UserAction

private const val WIDTH = 512
private const val HEIGHT = 512

class UIEntry(
    private val playerId: String,
    private val actionConsumer: UIEntry.(UserAction) -> Unit
) {
    private lateinit var loadingScreen: LoadingScreen
    private lateinit var gameWorld: GameWorld

    suspend fun init(callback: () -> Unit) {
        Korge(width = WIDTH, height = HEIGHT, targetFps = 60.0, debug = false) {
            loadingScreen = LoadingScreen(WIDTH, HEIGHT)
            addChild(loadingScreen)

            ResourceManager.init()

            gameWorld = GameWorld { this@UIEntry.actionConsumer(it) }.init(WIDTH, HEIGHT)
            gameWorld.visible = false
            addChild(gameWorld)

            callback()
        }
    }

    fun showGameWorld() {
        loadingScreen.visible = false
        gameWorld.visible = true

        updateGameWorld()
    }

    fun updateGameWorld() {
        gameWorld.setGameState(playerId, gameState)
    }
}

