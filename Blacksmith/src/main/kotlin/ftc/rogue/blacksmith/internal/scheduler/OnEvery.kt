package ftc.rogue.blacksmith.internal.scheduler

import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.units.TimeUnit
import ftc.rogue.blacksmith.util.SignalEdgeDetector

class OnEvery(
    val condition: () -> Boolean,
) {
    fun single(): OnEveryNth {
        return nth(1)
    }

    fun other(): OnEveryNth {
        return nth(2)
    }

    fun third(): OnEveryNth {
        return nth(3)
    }

    fun nth(n: Int): OnEveryNth {
        return OnEveryNth(condition, n)
    }
}

class OnEveryNth(
    val condition: () -> Boolean,
    val n: Int,
) {
    fun timeBeingTrue(): OnEveryTimeBeingX {
        return OnEveryTimeBeingX(condition, n)
    }

    fun timeBeingFalse(): OnEveryTimeBeingX {
        return OnEveryTimeBeingX({ !condition() }, n)
    }

    fun timeBecomingTrue(): OnEveryTimeBeingX {
        val sed = SignalEdgeDetector(condition)
        return OnEveryTimeBeingX({ sed.update(); sed.risingEdge() }, n)
    }

    fun timeBecomingFalse(): OnEveryTimeBeingX {
        val sed = SignalEdgeDetector(condition)
        return OnEveryTimeBeingX({ sed.update(); sed.fallingEdge() }, n)
    }
}

class OnEveryTimeBeingX(
    val condition: () -> Boolean,
    val n: Int,
    val offset: Int = 0,
) {
    fun extendFor(x: Int): OnEveryTimeBeingXDoYFor {
        return OnEveryTimeBeingXDoYFor(condition, n, x, offset)
    }

    fun doUntil(extendCondition: (Boolean, Long) -> Boolean): OnEveryTimeBeingXDoExtraIterationsUntil {
        return OnEveryTimeBeingXDoExtraIterationsUntil(condition, n, extendCondition, offset)
    }

    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
        return OnEveryImpl(condition, n, offset, untilCondition = untilCondition)
    }

    fun forever(): OnEveryImpl {
        return until { false }
    }

    fun withOffset(offset: Int): OnEveryTimeBeingX {
        return OnEveryTimeBeingX(condition, n, offset)
    }
}

class OnEveryTimeBeingXDoYFor(
    val condition: () -> Boolean,
    val n: Int,
    val x: Int,
    val offset: Int,
) {
    fun iterations(): OnEveryTimeBeingXDoYForZExtraIterations {
        if (n < 0) {
            throw IllegalArgumentException("Can't extend for negative ($x) iterations")
        }

        return OnEveryTimeBeingXDoYForZExtraIterations(condition, n, x, offset)
    }

    fun milliseconds(): OnEveryTimeBeingXDoYForZTime {
        if (n < 0) {
            throw IllegalArgumentException("Can't extend for negative ($x) time")
        }

        return time(TimeUnit.MILLISECONDS)
    }

    fun seconds(): OnEveryTimeBeingXDoYForZTime {
        if (n < 0) {
            throw IllegalArgumentException("Can't extend for negative ($x) time")
        }

        return time(TimeUnit.SECONDS)
    }

    fun time(unit: TimeUnit): OnEveryTimeBeingXDoYForZTime {
        return OnEveryTimeBeingXDoYForZTime(condition, n, unit.toMs(x), offset)
    }
}

class OnEveryTimeBeingXDoYForZExtraIterations(
    val condition: () -> Boolean,
    val n: Int,
    val iterations: Int,
    val offset: Int,
) {
    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
        var iterOffset = 0L
        var condWasTrue = true

        val extendCondition = { condIsTrue: Boolean, curr: Long ->
            if (!condIsTrue && condWasTrue) {
                iterOffset = curr
            }

            condWasTrue = condIsTrue
            curr - iterOffset < iterations
        }

        return OnEveryImpl(condition, n, offset, extendCondition, untilCondition = untilCondition)
    }

    fun forever(): OnEveryImpl {
        return until { false }
    }
}

class OnEveryTimeBeingXDoYForZTime(
    val condition: () -> Boolean,
    val n: Int,
    val ms: Int,
    val offset: Int,
) {
    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
        var timeOffset = 0L
        var condWasTrue = true

        val extendCondition = { condIsTrue: Boolean, _: Long ->
            if (!condIsTrue && condWasTrue) {
                timeOffset = System.currentTimeMillis()
            }

            condWasTrue = condIsTrue
            System.currentTimeMillis() - timeOffset < ms
        }

        return OnEveryImpl(condition, n, offset, extendCondition, untilCondition = untilCondition)
    }

    fun forever(): OnEveryImpl {
        return until { false }
    }
}

class OnEveryTimeBeingXDoExtraIterationsUntil(
    val condition: () -> Boolean,
    val n: Int,
    val extendCondition: (Boolean, Long) -> Boolean,
    val offset: Int,
) {
    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
        return OnEveryImpl(condition, n, offset, { b, l -> !extendCondition(b, l) }, untilCondition = untilCondition)
    }

    fun forever(): OnEveryImpl {
        return until { false }
    }
}

class OnEveryImpl(
    val condition: () -> Boolean,
    val requiredTrueStreak: Int,
    val offset: Int,
    val extendCondition: (Boolean, Long) -> Boolean = { _, _ -> false },
    val untilCondition: (Long) -> Boolean = { false },
) : Schedulable {
    init {
        hook()
    }

    private var totalCalls = 0L
    private var trueStreak = 0
    private var totalTrues = 0

    private var canBeExtended = false

    private var actions = mutableSetOf<Runnable>()

    fun execute(action: Runnable) = this.also {
        actions += action
    }

    override fun tick() {
        if (untilCondition(totalCalls)) {
            destroy()
            return
        }

        totalCalls++

        val conditionIsTrue = condition()

        if (conditionIsTrue) {
            trueStreak++
            totalTrues++
        }

        if (totalTrues <= offset) {
            return
        }

        val canExtendAction = extendCondition(conditionIsTrue && trueStreak >= requiredTrueStreak, totalCalls)
        val shouldExtendAction = canBeExtended && canExtendAction

        if (trueStreak < requiredTrueStreak && !shouldExtendAction) {
            canBeExtended = false
            return
        }

        if (conditionIsTrue && !shouldExtendAction) {
            trueStreak = 0
        }

        if (conditionIsTrue || shouldExtendAction) {
            canBeExtended = true
            actions.forEach(Runnable::run)
        }
    }

    override fun destroy() {
        actions.clear()
        Scheduler.unhook(this)
    }
}
