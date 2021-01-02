package ui

import com.soywiz.klock.milliseconds
import com.soywiz.korge.Korge
import com.soywiz.korge.input.mouse
import com.soywiz.korge.tiled.readTiledMap
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.interpolation.Easing
import com.soywiz.korma.interpolation.interpolate
import state.*
import state.entity.PersonalInfo
import state.entity.User
import state.gen.PlayerState
import java.lang.Math.abs

private const val WIDTH = 512
private const val HEIGHT = 512

actual object UIEntry {
    actual suspend operator fun invoke() = Korge(width = WIDTH, height = HEIGHT, targetFps = 60.0, debug = false) {
        val tiledMap = resourcesVfs["gfx/sample.tmx"].readTiledMap()

        val animationBitmap = resourcesVfs["gfx/NPC_test.png"].readBitmap()

        val animationLeft = SpriteAnimation(
            spriteMap = animationBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 96,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        val animationRight = SpriteAnimation(
            spriteMap = animationBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 32,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        val animationUp = SpriteAnimation(
            spriteMap = animationBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 64,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        val animationDown = SpriteAnimation(
            spriteMap = animationBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 0,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        val playerX = WIDTH / 2
        val playerY = HEIGHT / 2
        val player = PlayerState(
            "playerId",
            Position(playerX.toDimension(), playerY.toDimension()),
            VariableWithEmptyValue.empty(),
            VariableWithEmptyValue.empty(),
            PersonalInfo(3f),
            User("playerId", "playerName")
        )

        fixedSizeContainer(WIDTH, HEIGHT, clip = true) {
            xy(0, 0)

            tiledMapView(tiledMap) {}

            val playerSprite = sprite(animationDown).apply {
                spriteDisplayTime = 200.milliseconds // for animation
                addUpdater {
                    xy(player.x - 8, player.y - 16)
                }
            }

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

                    val initialPlayerPosition = player.position.copy()
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
                            if (d.x > 0 && d.x > abs(d.y)) playerSprite.playAnimation(animationRight)
                            if (d.x < 0 && -d.x > abs(d.y)) playerSprite.playAnimation(animationLeft)
                            if (d.y > 0 && d.y > abs(d.x)) playerSprite.playAnimation(animationDown)
                            if (d.y < 0 && -d.y > abs(d.x)) playerSprite.playAnimation(animationUp)
                        }
                    }

                    println("target (${targetPoint.x}, ${targetPoint.y}) player ${player.position}")
                }
            }
        }
    }
}

