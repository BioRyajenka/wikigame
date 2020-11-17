package server.chunking

import core.world.entities.Entity
import core.world.IntPosition

class Chunk(val position: IntPosition, val entities: MutableList<Entity>)
