package ftc.rogue.blacksmith.util.meepmeep.utils

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.entity.Entity
import java.lang.reflect.Proxy

interface ReflectedEntity {
    val tag: String

    val meepMeep: MeepMeep

    fun render(gfx: GraphicsHelper, canvasWidth: Int, canvasHeight: Int)

    fun getZIndex(): Int = 0

    fun setZIndex(idx: Int) {}

    fun update(deltaTime: Long) {}

    fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {}

    fun asEntity(): Entity {
        val e = Entity::class.java

        val helper = GraphicsHelper(meepMeep)

        return Proxy.newProxyInstance(e.classLoader, arrayOf(e)) { _, method, args ->
            when (method.name) {
                "getMeepMeep" -> {
                    meepMeep
                }

                "getTag" -> {
                    tag
                }

                "getZIndex" -> {
                    getZIndex()
                }

                "setZIndex" -> {
                    setZIndex(args[0] as Int)
                }

                "render" -> {
                    render(helper, args[1] as Int, args[2] as Int)
                }

                "update" -> {
                    update(args[0] as Long)
                }

                "setCanvasDimensions" -> {
                    setCanvasDimensions(args[0] as Double, args[1] as Double)
                }

                else -> null
            }
        } as Entity
    }
}
