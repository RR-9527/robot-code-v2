@file:Suppress("SpellCheckingInspection")

package ftc.rogue.blacksmith.internal.scheduler

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.internal.util.Consumer
import ftc.rogue.blacksmith.internal.util.consume
import ftc.rogue.blacksmith.listeners.Listener

@PublishedApi
internal class SchedulerInternal {
    val listeners = mutableSetOf<Listener>()

    var beforeEach = Runnable {}

    fun launch(opmode: LinearOpMode, afterEach: Runnable) {
        emit(Scheduler.STARTING_MSG)

        while (opmode.opModeIsActive() && !opmode.isStopRequested) {
            updateListenersSet()

            beforeEach.run()
            tick()
            afterEach.run()
        }
    }

    fun launchOnStart(opmode: LinearOpMode, afterEach: Runnable) {
        opmode.waitForStart()
        launch(opmode, afterEach)
    }

    inline fun launchManually(condition: () -> Boolean, afterEach: Runnable = Runnable {}) {
        emit(Scheduler.STARTING_MSG)

        while (condition()) {
            updateListenersSet()

            beforeEach.run()
            tick()
            afterEach.run()
        }
    }

    fun debug(opmode: LinearOpMode, afterEach: Consumer<SchedulerDebugInfo>) {
        val elapsedTime = ElapsedTime()

        launch(opmode) {
            val time = elapsedTime.milliseconds()

            elapsedTime.reset()

            val debugInfo = SchedulerDebugInfo(
                time, listeners.size, messages.size
            )

            afterEach.consume(debugInfo)
        }
    }

    fun nuke(toNuke: Int) {
        updateListenersSet()

        if (toNuke and All.inv() > 0) {
            throw IllegalArgumentException("Bitflag uses number that isn't Listener, Messages, BeforeEach, nor All (1, 2, 4, or 7)")
        }

        if (toNuke and Listeners == Listeners) {
            listeners.forEach {
                it.destroy()
            }
            listeners.clear()
        }

        if (toNuke and Messages == Messages) {
            messages.clear()
        }

        if (toNuke and BeforeEach == BeforeEach) {
            beforeEach = Runnable {}
        }
    }

    private val messages = mutableMapOf<Any, MutableList<Runnable>>()

    fun on(message: Any, callback: Runnable) {
        messages.getOrPut(message, ::ArrayList) += callback
    }

    fun emit(message: Any) {
        messages[message]?.forEach(Runnable::run)
    }

    private val listenersToAdd = mutableSetOf<Listener>()
    private val listenersToRemove = mutableSetOf<Listener>()

    fun hookListener(listener: Listener) {
        listenersToAdd += listener
    }

    fun unhookListener(listener: Listener) {
        listenersToRemove += listener
    }

    @JvmSynthetic
    @PublishedApi
    internal fun updateListenersSet() {
        listeners += listenersToAdd
        listenersToAdd.clear()
        listeners -= listenersToRemove
        listenersToRemove.clear()
    }

    @JvmSynthetic
    @PublishedApi
    internal fun tick() {
        listeners.forEach(Listener::tick)
    }
}
