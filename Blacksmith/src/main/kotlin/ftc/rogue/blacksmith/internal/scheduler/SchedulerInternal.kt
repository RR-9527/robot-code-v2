@file:Suppress("SpellCheckingInspection")

package ftc.rogue.blacksmith.internal.scheduler

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.internal.util.Consumer
import ftc.rogue.blacksmith.internal.util.consume

@PublishedApi
internal class SchedulerInternal {
    @PublishedApi
    @get:JvmSynthetic
    internal val schedulables = mutableSetOf<Schedulable>()

    @PublishedApi
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var beforeEach = Runnable {}

    fun launch(opmode: LinearOpMode, afterEach: Runnable) {
        launchManually({ opmode.opModeIsActive() && !opmode.isStopRequested }, afterEach)
    }

    fun launchOnStart(opmode: LinearOpMode, afterEach: Runnable) {
        opmode.waitForStart()
        launch(opmode, afterEach)
    }

    inline fun launchManually(condition: () -> Boolean, afterEach: Runnable) {
        emit(Scheduler.STARTING_MSG)

        while (condition()) {
            updateListenersSet()

            beforeEach.run()
            tick()
            afterEach.run()
        }
    }

    inline fun debug(condition: () -> Boolean, afterEach: Consumer<SchedulerDebugInfo>) {
        val elapsedTime = ElapsedTime()

        launchManually(condition) {
            val time = elapsedTime.milliseconds()

            elapsedTime.reset()

            val debugInfo = SchedulerDebugInfo(
                time, schedulables.size, messages.size
            )

            afterEach.consume(debugInfo)
        }
    }

    fun nuke(toNuke: Array<out NukeFlag>) {
        updateListenersSet()

        val nukeFlag = toNuke.reduce(NukeFlag::or)

        if ((nukeFlag and !All).flag > 0) {
            throw IllegalArgumentException("Bitflag uses number that isn't Listener, Messages, BeforeEach, nor All (1, 2, 4, or 7)")
        }

        if (nukeFlag and Schedulables == Schedulables) {
            schedulables.forEach {
                it.destroy()
            }
            schedulables.clear()
        }

        if (nukeFlag and Messages == Messages) {
            messages.clear()
        }

        if (nukeFlag and BeforeEach == BeforeEach) {
            beforeEach = Runnable {}
        }
    }

    @PublishedApi
    @get:JvmSynthetic
    internal val messages = mutableMapOf<Any, MutableList<Runnable>>()

    fun on(message: Any, callback: Runnable) {
        messages.getOrPut(message, ::ArrayList) += callback
    }

    fun emit(message: Any) {
        messages[message]?.forEach(Runnable::run)
    }

    private val schedulablesToAdd = mutableSetOf<Schedulable>()
    private val schedulablesToRemove = mutableSetOf<Schedulable>()

    fun hook(schedulable: Schedulable) {
        schedulablesToAdd += schedulable
    }

    fun unhook(schedulable: Schedulable) {
        schedulablesToRemove += schedulable
    }

    @JvmSynthetic
    @PublishedApi
    internal fun updateListenersSet() {
        schedulables += schedulablesToAdd
        schedulablesToAdd.clear()
        schedulables -= schedulablesToRemove
        schedulablesToRemove.clear()
    }

    @JvmSynthetic
    @PublishedApi
    internal fun tick() {
        schedulables.forEach(Schedulable::tick)
    }
}
