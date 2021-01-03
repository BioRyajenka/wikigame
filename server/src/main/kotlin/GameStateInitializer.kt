import com.gitlab.mvysny.konsumexml.getValueInt
import com.gitlab.mvysny.konsumexml.konsumeXml
import state.IntPosition
import state.MapCell
import state.Position
import state.VariableWithEmptyValue
import state.entity.PersonalInfo
import state.entity.User
import state.gen.*
import kotlin.properties.Delegates

fun createInitialGameState(): GameState {
    var width: Int
    var height: Int
    var tileWidth by Delegates.notNull<Int>()
    var tileHeight by Delegates.notNull<Int>()

    val layers = mutableListOf<Map<IntPosition, Int>>()

    GameServer::class.java.getResource("map.tmx").readText().konsumeXml().apply {
        child("map") {
            width = attributes.getValueInt("width")
            height = attributes.getValueInt("height")
            tileWidth = attributes.getValueInt("tilewidth")
            tileHeight = attributes.getValueInt("tileheight")
            val layersNum = attributes.getValueInt("nextlayerid") - 1

            repeat(layersNum) { layers.add(mutableMapOf()) }

            child("tileset") {}
            children("layer") {
                val layerId = attributes.getValueInt("id")
                val layerStringData = childText("data")

                val layer = mutableMapOf<IntPosition, Int>()
                layers[layerId - 1] = layer
                val rawData = layerStringData.split(',').map { it.trim().toInt() }
                repeat(height) { i ->
                    repeat(width) { j ->
                        layer[IntPosition(i, j)] = rawData[i * width + j]
                    }
                }
            }
        }
    }

    val mapState = MapState(layers.first().keys.associateWith { cellPos ->
        MapCell(layers.map { it.getValue(cellPos) })
    }.toMutableMap())

    val playersPos = listOf(
        IntPosition(5, 5),
        IntPosition(5, 15)
    )
    val players = mutableListOf<PlayerState>()
    players += playersPos.mapIndexed { i, playerPos ->
        val playerId = "player${i + 1}"
        val playerName = "Player ${i + 1}"

        PlayerState(
            playerId,
            Position(1f * playerPos.j * tileWidth, 1f * playerPos.i * tileHeight),
            VariableWithEmptyValue.empty(),
            VariableWithEmptyValue.empty(),
            PersonalInfo(3f),
            User(playerId, playerName)
        )
    }

    return GameState(
        mutableMapOf(),
        mutableMapOf(),
        players.associateBy { it.id }.toMutableMap(),
        mapState
    )
}
