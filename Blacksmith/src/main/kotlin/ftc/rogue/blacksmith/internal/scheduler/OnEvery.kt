package ftc.rogue.blacksmith.internal.scheduler

import ftc.rogue.blacksmith.listeners.OnImpl
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
) {
    fun extendFor(x: Int): OnEveryTimeBeingXDoYFor {
        return OnEveryTimeBeingXDoYFor(condition, n, x)
    }

    fun doUntil(extendCondition: (Boolean, Long) -> Boolean): OnEveryTimeBeingXDoExtraIterationsUntil {
        return OnEveryTimeBeingXDoExtraIterationsUntil(condition, n, extendCondition)
    }

    fun until(untilCondition: (Long) -> Boolean): OnImpl {
        return OnImpl(condition, n, untilCondition = untilCondition)
    }

    fun forever(): OnImpl {
        return until { false }
    }
}

class OnEveryTimeBeingXDoYFor(
    val condition: () -> Boolean,
    val n: Int,
    val x: Int,
) {
    fun iterations(): OnEveryTimeBeingXDoYForZExtraIterations {
        if (n < 0) {
            throw IllegalArgumentException("Can't extend for negative ($x) iterations")
        }

        return OnEveryTimeBeingXDoYForZExtraIterations(condition, n, x)
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
        return OnEveryTimeBeingXDoYForZTime(condition, n, unit.toMs(x))
    }
}

class OnEveryTimeBeingXDoYForZExtraIterations(
    val condition: () -> Boolean,
    val n: Int,
    val iterations: Int,
) {
    fun until(untilCondition: (Long) -> Boolean): OnImpl {
        var offset = 0L
        var condWasTrue = true

        val extendCondition = { condIsTrue: Boolean, curr: Long ->
            if (!condIsTrue && condWasTrue) {
                offset = curr
            }

            condWasTrue = condIsTrue
            curr - offset < iterations
        }

        return OnImpl(condition, n, extendCondition, untilCondition = untilCondition)
    }

    fun forever(): OnImpl {
        return until { false }
    }
}

class OnEveryTimeBeingXDoYForZTime(
    val condition: () -> Boolean,
    val n: Int,
    val ms: Int,
) {
    fun until(untilCondition: (Long) -> Boolean): OnImpl {
        var offset = 0L
        var condWasTrue = true

        val extendCondition = { condIsTrue: Boolean, _: Long ->
            if (!condIsTrue && condWasTrue) {
                offset = System.currentTimeMillis()
            }

            condWasTrue = condIsTrue
            System.currentTimeMillis() - offset < ms
        }

        return OnImpl(condition, n, extendCondition, untilCondition = untilCondition)
    }

    fun forever(): OnImpl {
        return until { false }
    }
}

class OnEveryTimeBeingXDoExtraIterationsUntil(
    val condition: () -> Boolean,
    val n: Int,
    val extendCondition: (Boolean, Long) -> Boolean,
) {
    fun until(untilCondition: (Long) -> Boolean): OnImpl {
        return OnImpl(condition, n, { b, l -> !extendCondition(b, l) }, untilCondition = untilCondition)
    }

    fun forever(): OnImpl {
        return until { false }
    }
}
