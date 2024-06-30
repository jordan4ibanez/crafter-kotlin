package engine.entity

import engine.tick.Tick
import org.joml.Vector2f
import org.joml.Vector3f

internal const val interpolationSnappiness = Tick.GOAL
internal val vector3Worker = Vector3f()
internal val vector2Worker = Vector2f()

internal val vel2d = Vector2f()
internal val diff = Vector2f()
internal val accelerationWorker = Vector3f()
internal val goalVel = Vector2f()
