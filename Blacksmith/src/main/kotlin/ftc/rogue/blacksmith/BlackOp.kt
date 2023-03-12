@file:Suppress("HasPlatformType", "UNNECESSARY_NOT_NULL_ASSERTION")

package ftc.rogue.blacksmith

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import ftc.rogue.blacksmith.internal.blackop.CreateOnGoInternal
import ftc.rogue.blacksmith.internal.blackop.injectCreateOnGoFields
import ftc.rogue.blacksmith.internal.blackop.injectEvalOnGoFields
import ftc.rogue.blacksmith.internal.util.NotNull
import ftc.rogue.blacksmith.listeners.ReforgedGamepad

/**
 * [**LINK TO OFFICIAL DOCS (click on me) (please read) (I like cars)**](https://blacksmithftc.vercel.app/black-op/overview)
 *
 * A nicer version of LinearOpMode, please read docs about this class if you wish to use it,
 * which I recommend, *especially* if you use Kotlin.
 *
 * Still nice for Java too.
 *
 * Provides some nice utilities for writing OpModes, such as:
 *  - A global mTelemetry which logs to both driver station and FTCDashboard
 *  - a global hwMap, short for hardwareMap
 *  - Emits a message (`BlackOp.STARTING_MSG`) which can be subscribed to do something with `Scheduler.on(BlackOp.STARTING_MSG, () -> { ... })`
 *  - Allows use of `@CreateOnGo` for Java (read docs for this, it is quite nice)
 *  - Allows use of `createOnGo/evalOnGo` for Kotlin (read docs for this, it is quite nice)
 *  - Sounds kinda cool Idk
 *
 *  i like cars
 */
abstract class BlackOp : LinearOpMode() {
    /**
     * The method to override in place of runOpMode.
     *
     * goo.
     */
    abstract fun go()

    init {
        Scheduler.nuke()
    }

    /**
     * Please override `go()` instead of this method.
     */
    final override fun runOpMode() {
        hwMap = hardwareMap
        mTelemetry = MultipleTelemetry(telemetry, FtcDashboard.getInstance().telemetry)

        injectCreateOnGoFields()
        injectEvalOnGoFields()

        Scheduler.emit(STARTING_MSG)

        go!!!!!!!!!!!!!!!!
    }

    companion object {
        /**
         * A global telemetry holder, which logs to both the driver station and the dashboard.
         *
         * Is only assigned to a driver station telemetry when a BlackOp instance is initialized.
         *
         * Do not use directly before a [BlackOp] instance is initialized, or it won't be the right
         * telemetry
         */
        @JvmStatic
        fun mTelemetry() = mTelemetry

        @get:JvmSynthetic
        var mTelemetry by NotNull<MultipleTelemetry>(errorMsg = "Can't access mTelemetry before a BlackOp instance is initialized!")
            private set

        /**
         * A global hardware map holder, and is also short for `hardwareMap`.
         *
         * Is only assigned to a hardware map when a BlackOp instance is initialized.
         *
         * Do not use directly before a [BlackOp] instance is initialized, or it won't be the right
         * hwMap
         */
        @JvmStatic
        fun hwMap() = hwMap

        @get:JvmSynthetic
        var hwMap by NotNull<HardwareMap>(errorMsg = "Can't access hwMap before a BlackOp instance is initialized!")
            private set

        @JvmStatic
        fun quickLog(line: String) {
            mTelemetry.addLine(line)
            mTelemetry.update()
        }

        /**
         * The starting message emitted when runOpMode() is called on a BlackOp.
         *
         * ```java
         * Scheduler.on(BlackOp.STARTING_MSG, () -> {
         *    // do something when opmode starts
         * });
         */
        const val STARTING_MSG = 3248023743480398723L
    }

    fun getReforgedGamepad1(): ReforgedGamepad {
        return ReforgedGamepad(gamepad1)
    }

    fun getReforgedGamepad2(): ReforgedGamepad {
        return ReforgedGamepad(gamepad2)
    }

    // -- KOTLIN ONLY BELOW (Check out @CreateOnGo and @EvalOnGo for Java) --

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/black-op/create-on-go-kt)
     */
    @JvmSynthetic
    protected fun <T : Any> evalOnGo(constructor: () -> T) =
        CreateOnGoInternal(constructor)

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/black-op/create-on-go-kt)
     *
     * Functionally the same as the below, just allows you to pass in a single lambda without
     * forcing it to be in a parenthesized argument list.
     */
    protected inline fun <reified T : Any> createOnGo(noinline arg: () -> Any) =
        CreateOnGoInternal<T>(*arrayOf(arg)) // Needs array spread or type checker errors

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/black-op/create-on-go-kt)
     */
    protected inline fun <reified T : Any> createOnGo(vararg args: () -> Any) =
        CreateOnGoInternal<T>(*args)

    // Shh
    private val go get() = go()
}
