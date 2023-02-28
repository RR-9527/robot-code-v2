@file:Suppress("ClassName")

package ftc.rogue.blacksmith.listeners

import ftc.rogue.blacksmith.units.TimeUnit
import ftc.rogue.blacksmith.Scheduler

// -----------------------------------------------------------------------------------------------
// Dear reader-
//
// Put bluntly, this class is hideous.
//
// I implore you to not look too far into it's implementation for fear of gaining an obsessive
// want to pour copious amounts of bleach into your own eyes.
//
// This has been your first and only warning.
// Just read the docs instead.
//
// Godspeed.
// -----------------------------------------------------------------------------------------------

/**
 * [Docs link](https://blacksmithftc.vercel.app/scheduler-api/timer)
 *
 * A timer that can be used to schedule actions to be performed at a specific time.
 *
 * @author KG
 *
 * @see Scheduler
 * @see Listener
 * @see ReforgedGamepad
 */
class Timer @JvmOverloads constructor(
    length: Long,
    startPending: Boolean = false,
    unit: TimeUnit = TimeUnit.MILLISECONDS
) {
    private val listener0 = Listener { !isPending && System.currentTimeMillis() - startTime >= unit.toMs(length) }

    private val listener1 = Listener { System.currentTimeMillis() - startTime >= unit.toMs(length) }

    private val length = unit.toMs(length)

    private var startTime = System.currentTimeMillis()

    private var isPending = startPending

    /**
     * Schedules the given action to run while the timer is running or pending.
     * @param action The action to run.
     * @return The timer instance.
     */
    fun whileWaiting(callback: Runnable) = this.also { listener0.whileLow(callback) }

    /**
     * Schedules the given action to run while the timer is running and not yet finsihed.
     * @param action The action to run.
     * @return The timer instance.
     */
    fun whileRunning(callback: Runnable) = this.also { listener1.whileLow(callback) }

    /**
     * Schedules the given action to run once when the timer is finished.
     * @param action The action to run.
     * @return The timer instance.
     */
    fun onDone(callback: Runnable) = this.also { listener0.onRise(callback) }

    /**
     * Starts the timer if not already running, resets the timer if already running.
     */
    fun start() {
        isPending = false
        startTime = System.currentTimeMillis()
    }

    /**
     * Finishes the timer if running, does nothing if not running.
     */
    fun finishPrematurely() {
        startTime = System.currentTimeMillis() + length
    }

    /**
     * Sets the timer to pending if `true` or nothing is passed, or makes the timer
     * not pending if `false` is passed.
     */
    @JvmOverloads
    fun setPending(newState: Boolean = true) {
        isPending = newState
    }

    /**
     * Destroys the timer and sets it pending. `onDone` will be called.
     */
    fun destroy() {
        isPending = true
        listener0.destroy()
    }

    /**
     * Figure it out for yourself given the method name.
     */
    fun isDone() = listener0.condition()

    /**
     * Used for using `after` in Java, read the docs please for more info about this very
     * useful tool.
     */
    fun after(time: Long) = ftc.rogue.blacksmith.listeners.after(time)
}

// -------------------------- Made you look --------------------------
//
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⢔⣂⣤⣤⣤⣾⣭⣭⣍⣀⣰⠢⠀⡀⠀⠀⠀⠀⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡠⣪⡿⠉⠁⠈⠀⠁⠀⠀⠈⠉⠙⠳⣶⣌⠠⡀⠀⠀⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⢡⡿⠁⠀⠀⠀⢀⡀⠀⠀⠀⠀⠀⠀⠈⢻⣧⠐⠄⠀⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣄⣿⠇⢀⣴⣿⠛⠛⡛⠛⠛⠛⠻⢶⣄⠀⠀⠹⣧⠰⡀⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⣸⡏⠀⠘⣿⣎⠒⠄⣈⣀⡀⠀⠄⣤⣿⠇⠀⠀⢹⣆⢁⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⢡⣿⠁⠀⠀⠈⠙⠷⠶⠶⠶⠶⠶⠟⠋⠁⠀⠀⠀⠈⣿⢸⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣾⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠸⠃
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⡄⡿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⡄⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⢠⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇⡇
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠈⣾⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⡇⠁
//              ⠀⠀⠀⠀⠀⠀⠀⠀⣇⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⡇⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠰⢸⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣷⠁
//              ⠀⠀⠀⠀⠀⠀⠀⠄⣿⠀⠀⠀⠀⠀⣀⣤⡴⠶⠶⠶⠶⢦⣄⡀⠀⠀⠀⠀⠀⠀⠀⣿⠀
//              ⠀⠀⠀⢀⡀⠀⢠⢱⣿⠀⠀⠀⠀⢸⡟⡑⠈⠁⠀⠀⠈⠐⠽⣷⠀⠀⠀⠀⠀⠀⠀⣿⠀
//              ⢀⠀⣪⣵⢮⣭⣥⣾⡇⠀⠀⠀⠀⠸⡇⠃⠀⠀⠀⠀⠀⠀⡒⣿⠀⠀⠀⠀⠀⠀⠀⣏⠀
//              ⠀⣾⠁⠀⠀⠀⠀⠉⠀⠀⠀⠀⠀⠀⣿⢸⠀⠀⠀⣀⣀⠀⣱⡏⠀⠀⠀⠀⠀⠀⠀⡿⠘
//              ⠐⠼⢷⣄⡀⠀⠀⠀⠀⣀⣀⣠⣴⠾⠋⠄⠠⢴⣬⠶⠶⠒⠿⠇⠀⠀⠀⠀⠀⠀⠀⡇⡇
//              ⠀⠈⠀⠭⠙⢻⠿⡛⠛⠋⠭⠩⠕⠈⠀⠀⡄⣿⡃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣸⢇⠁
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⠙⡳⢦⣄⣀⣀⣀⣀⣤⣤⡴⢾⡋⠅⠈⠀
//              ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⠀⠚⠉⠩⠍⠓⠒⠀⠁⠀⠀⠀⠀
// -------------------------------------------------------------------

/**
 * Read the docs please about this very useful tool.
 */
class after(val time: Long) {
    fun milliseconds(callback: Runnable) = unit(TimeUnit.MILLISECONDS, callback)

    fun seconds(callback: Runnable) = unit(TimeUnit.SECONDS, callback)

    fun unit(unit: TimeUnit, callback: Runnable) = Timer(time, unit = unit)
        .run {
            onDone(callback)
            onDone(::destroy)
            start()
        }
}
