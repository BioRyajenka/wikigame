package ui

import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.format.readBitmap
import com.soywiz.korinject.AsyncDependency
import com.soywiz.korio.file.std.resourcesVfs

object ResourceManager : AsyncDependency {
    private val additionalResourceInitializers = mutableListOf<AsyncDependency>()
    private var initialized = false

    override suspend fun init() {
        initialized = true

        additionalResourceInitializers.add(PlayerSpriteResources)

        additionalResourceInitializers.forEach { it.init() }
    }

    fun addResourceInitializer(resourceInitializer: AsyncDependency) {
        check(!initialized)
        additionalResourceInitializers += resourceInitializer
    }
}
