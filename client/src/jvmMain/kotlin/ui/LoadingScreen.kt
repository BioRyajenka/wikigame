package ui

import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.text
import com.soywiz.korge.view.xy

object LoadingScreen : Container(), View.Reference {
    suspend operator fun invoke(screenWidth: Int, screenHeight: Int): LoadingScreen {
        text("Loading...") {
            xy(screenWidth / 2, screenHeight / 2)
        }
        return this
    }
}
