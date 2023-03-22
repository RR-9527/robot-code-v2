package ftc.rogue.blacksmith.util.meepmeep

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import java.lang.reflect.Proxy

fun scrollWheelMovedListener(mm: MeepMeep, callback: (Int, Boolean, Boolean) -> Unit) {
    val instance = Class.forName("java.awt.event.MouseWheelListener")

    val mouseWheelEventClass = Class.forName("java.awt.event.MouseWheelEvent")
    val getWheelRotation = mouseWheelEventClass.getMethod("getWheelRotation")
    val getModifiers = mouseWheelEventClass.getMethod("getModifiers")

    val listener = Proxy.newProxyInstance(instance.classLoader, arrayOf(instance)) { _, method, args ->
        if (method.name != "mouseWheelMoved")
            return@newProxyInstance null

        callback(
            getWheelRotation.invoke(args[0]) as Int,
            getModifiers.invoke(args[0]) as Int and 1 == 1,
            getModifiers.invoke(args[0]) as Int and 8 == 8,
        )
    }

    mm.canvas::class.java
        .methods
        .first { it.name == "addMouseWheelListener" }
        .invoke(mm.canvas, listener)
}

fun mouseDraggedListener(mm: MeepMeep, callback: (Int, Int) -> Unit) {
    val instance = Class.forName("java.awt.event.MouseMotionListener")

    val mouseEvent = Class.forName("java.awt.event.MouseEvent")
    val getX = mouseEvent.getMethod("getX")
    val getY = mouseEvent.getMethod("getY")

    val listener = Proxy.newProxyInstance(instance.classLoader, arrayOf(instance)) { _, method, args ->
        if (method.name != "mouseDragged")
            return@newProxyInstance null

        callback(
            getX.invoke(args[0]) as Int,
            getY.invoke(args[0]) as Int,
        )
    }

    mm.canvas::class.java
        .methods
        .first { it.name == "addMouseMotionListener" }
        .invoke(mm.canvas, listener)
}

fun getCanvasMouseSupplier(mm: MeepMeep): () -> Vector2d {
    val canvasMouseX = mm::class.java.getDeclaredField("canvasMouseX")
        .also { it.isAccessible = true }

    val canvasMouseY = mm::class.java.getDeclaredField("canvasMouseY")
        .also { it.isAccessible = true }

    return {
        Vector2d((canvasMouseX.get(mm) as Int).toDouble(), (canvasMouseY.get(mm) as Int).toDouble())
    }
}

fun keyPressedListener(mm: MeepMeep, callback: (Int) -> Unit) {
    val instance = Class.forName("java.awt.event.KeyListener")

    val keyEvent = Class.forName("java.awt.event.KeyEvent")
    val getKeyCode = keyEvent.getMethod("getKeyCode")

    val listener = Proxy.newProxyInstance(instance.classLoader, arrayOf(instance)) { _, method, args ->
        if (method.name != "keyPressed")
            return@newProxyInstance null

        callback(
            getKeyCode.invoke(args[0]) as Int,
        )
    }

    mm.canvas::class.java
        .methods
        .first { it.name == "addKeyListener" }
        .invoke(mm.canvas, listener)
}
