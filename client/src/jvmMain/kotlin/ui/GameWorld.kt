package ui

import TimeProvider
import com.soywiz.korge.input.mouse
import com.soywiz.korge.tiled.readTiledMap
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.*
import com.soywiz.korio.file.std.resourcesVfs
import state.*
import state.action.Move
import state.action.UserAction
import state.entity.PersonalInfo
import state.entity.User
import state.gen.GameState
import state.gen.PlayerState

class GameWorld(private val actionConsumer: (UserAction) -> Unit) : Container(), View.Reference {
    var player: PlayerState = createInitialDummyPlayer()

    suspend fun init(screenWidth: Int, screenHeight: Int): GameWorld {
        val tiledMap = resourcesVfs["gfx/sample.tmx"].readTiledMap()

        fixedSizeContainer(screenWidth, screenHeight, clip = true) {
            xy(0, 0)

            tiledMapView(tiledMap) {}

            val playerSprite = PlayerSprite(this@GameWorld::player)
            addChild(playerSprite)

            this.mouse {
                onUp { mouseEvent ->
                    if (mouseEvent.currentPosGlobal.x > this@fixedSizeContainer.width
                        || mouseEvent.currentPosGlobal.y > this@fixedSizeContainer.height) {
                        return@onUp
                    }

                    val targetPoint = Vector(
                        mouseEvent.currentPosGlobal.x - this@fixedSizeContainer.x,
                        mouseEvent.currentPosGlobal.y - this@fixedSizeContainer.y
                    )

                    val moveEvent = Move(
                        TimeProvider.currentTime,
                        player.personalInfo.movementSpeed,
                        targetPoint
                    )
                    actionConsumer(moveEvent)

                    /*val initialPlayerPosition = player.position.copy()
                    val length = (targetPoint - player.position).vectorLength
                    val time = length.milliseconds * 16 / player.personalInfo.movementSpeed.toDouble()

                    playerSprite.tween(time = time, easing = Easing.LINEAR) { ratio ->
                        val newPos = Vector(
                            ratio.interpolate(initialPlayerPosition.x, targetPoint.x),
                            ratio.interpolate(initialPlayerPosition.y, targetPoint.y)
                        )
                        val d = newPos - player.position
                        player.position = newPos

                        if (ratio >= 1.0) {
                            playerSprite.stopAnimation()
                            playerSprite.setFrame(0)
                        } else {
                            if (d.x > 0 && d.x > Math.abs(d.y)) playerSprite.playAnimation(animationRight)
                            if (d.x < 0 && -d.x > Math.abs(d.y)) playerSprite.playAnimation(animationLeft)
                            if (d.y > 0 && d.y > Math.abs(d.x)) playerSprite.playAnimation(animationDown)
                            if (d.y < 0 && -d.y > Math.abs(d.x)) playerSprite.playAnimation(animationUp)
                        }
                    }*/

                    println("target (${targetPoint.x}, ${targetPoint.y}) player ${player.position}")
                }
            }
        }

        return this
    }

    fun setGameState(playerId: String, gameState: GameState) {
        // TODO gameState.mapState

        player = gameState.players[playerId]!!
    }
}

fun createInitialDummyPlayer(): PlayerState {
    val playerX = 256
    val playerY = 256

    return PlayerState(
        "player1",
        Position(playerX.toDimension(), playerY.toDimension()),
        VariableWithEmptyValue.empty(),
        VariableWithEmptyValue.empty(),
        PersonalInfo(0.5f),
        User("player1", "playerName")
    )
}
