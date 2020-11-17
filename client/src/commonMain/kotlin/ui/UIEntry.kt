package ui

import com.soywiz.klock.milliseconds
import com.soywiz.korge.Korge
import com.soywiz.korge.input.mouse
import com.soywiz.korge.tiled.readTiledMap
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.math.abs

private const val WIDTH = 512 * 2
private const val HEIGHT = 512 * 2
private const val EPS = 1e-3

object UIEntry {
    suspend operator fun invoke() = Korge(width = WIDTH, height = HEIGHT) {
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

        val player = Player(WIDTH / 2, HEIGHT / 2, speed = 3.toDimension())

        fixedSizeContainer(WIDTH, HEIGHT, clip = true) {
            position(0, 0)

            val camera = camera {
                tiledMapView(tiledMap) {
                }
                addUpdater {
                    xy(player.x - WIDTH / 2, player.y - HEIGHT / 2)
                }
            }

            val playerSprite = sprite(animationDown).apply {
//        scale(3)
                spriteDisplayTime = 200.milliseconds // for animation
                addUpdater {
                    // - 8, - 16
                    xy(player.x - camera.x, player.y - camera.y)
                }
            }

            var targetPoint: Vector = player

            this.mouse {
                onClick {
                    up {
//                    println("up")
                    }
                    down {
//                    println("down")
                    }

                    targetPoint = Vector(
                        camera.x + it.currentPosGlobal.x,
                        camera.y + it.currentPosGlobal.y
                    )
                    println("target (${targetPoint.x}, ${targetPoint.y}) player $player)")
                }
            }

            addUpdater { time ->
                if (time == 0.milliseconds) return@addUpdater

                val scale = 16.milliseconds / time
                val disp = 2 * scale

                val dist = targetPoint - player
                var d = dist.copy()

                if (d.length < EPS) return@addUpdater

                if (d.x > 0 && d.x > abs(d.y)) playerSprite.playAnimation(animationRight)
                if (d.x < 0 && -d.x > abs(d.y)) playerSprite.playAnimation(animationLeft)
                if (d.y > 0 && d.y > abs(d.x)) playerSprite.playAnimation(animationDown)
                if (d.y < 0 && -d.y > abs(d.x)) playerSprite.playAnimation(animationUp)

                d = d / d.length * disp * player.speed
                if (d.length > dist.length) d = dist
                println("$player $targetPoint $d")
                player.x += d.x
                player.y += d.y
            }
        }
    }
}
