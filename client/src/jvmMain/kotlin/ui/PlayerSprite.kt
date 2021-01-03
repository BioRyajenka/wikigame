package ui

import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.Sprite
import com.soywiz.korge.view.SpriteAnimation
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.xy
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.format.readBitmap
import com.soywiz.korinject.AsyncDependency
import com.soywiz.korio.file.std.resourcesVfs
import state.Position
import state.action.Move
import state.gen.PlayerState
import state.x
import state.y
import ui.PlayerSpriteResources.animationDown

class PlayerSprite(playerGetter: () -> PlayerState): Sprite(animationDown) {
    init {
        spriteDisplayTime = 200.milliseconds // for animation
        addUpdater {
            val player = playerGetter()
            val activeAction = player.activeAction.getValue()

            if (activeAction is Move) {
                val currentTime = TimeProvider.currentTime
                xyWithShift(activeAction.calculateNewPosition(player, currentTime))
            } else {
                xyWithShift(player.position)
            }
        }
    }

    private fun xyWithShift(newPos: Position) {
        xy(newPos.x.toInt() - 8, newPos.y.toInt() - 16)
    }
//    fun moveTo()
}

object PlayerSpriteResources : AsyncDependency {
    lateinit var mapBitmap: Bitmap

    lateinit var animationLeft: SpriteAnimation
    lateinit var animationUp: SpriteAnimation
    lateinit var animationRight: SpriteAnimation
    lateinit var animationDown: SpriteAnimation

    override suspend fun init() {
        mapBitmap = resourcesVfs["gfx/NPC_test.png"].readBitmap()

        animationLeft = SpriteAnimation(
            spriteMap = mapBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 96,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        animationRight = SpriteAnimation(
            spriteMap = mapBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 32,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        animationUp = SpriteAnimation(
            spriteMap = mapBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 64,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )

        animationDown = SpriteAnimation(
            spriteMap = mapBitmap,
            spriteWidth = 16,
            spriteHeight = 32,
            marginTop = 0,
            marginLeft = 0,
            columns = 4,
            rows = 1
        )
    }
}
