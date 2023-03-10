package ftc.rogue.blacksmith.listeners

import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.internal.scheduler.OnEvery
import ftc.rogue.blacksmith.internal.scheduler.OnEveryTimeBeingX
import ftc.rogue.blacksmith.internal.scheduler.Schedulable
import ftc.rogue.blacksmith.util.SignalEdgeDetector

// --------------------------------
// | Hierarchy:
// | - theFirst()  |
// | - theSecond() |
// | - theThird()  |
// | - theNth(n)   :
// | ----- timeBeingTrue()
// | ----- timeBeingFalse()
// | ----- timeBecomingTrue()
// | ----- timeBecomingFalse()
// | - every():
// | --- single() |
// | --- second() |
// | --- third()  |
// | --- nth(n)   :
// | ----- timeBeingTrue()     |
// | ----- timeBeingFalse()    |
// | ----- timeBecomingTrue()  |
// | ----- timeBecomingFalse() :
// | ------- until(λn.b)
// | ------- doFor(n):
// | --------- iterations()
// | --------- milliseconds()
// | --------- seconds()
// | --------- time(u)
// | ------- doWhile(λn.b)
// | ------- forever()
// | - beingTrue():
// | --> every().single().timeBeingTrue()
// | - beingFalse():
// | --> every().single().timeBeingFalse()
// | - becomingTrue():
// | --> every().single().timeBecomingTrue()
// | - becomingFalse():
// | --> every().single().timeBecomingFalse()
// |
// | Finally:
// | - execute(λ)
// --------------------------------

class On(val condition: () -> Boolean) {
    fun theFirst(): OnTheNth {
        return OnTheNth(condition, n = 1)
    }

    fun theSecond(): OnTheNth {
        return OnTheNth(condition, n = 2)
    }

    fun theThird(): OnTheNth {
        return OnTheNth(condition, n = 3)
    }

    fun theNth(n: Int): OnTheNth {
        return OnTheNth(condition, n)
    }

    fun every(): OnEvery {
        return OnEvery(condition)
    }

    fun beingTrue(): OnEveryTimeBeingX {
        return every().single().timeBeingTrue()
    }

    fun beingFalse(): OnEveryTimeBeingX {
        return every().single().timeBeingFalse()
    }

    fun becomingTrue(): OnEveryTimeBeingX {
        return every().single().timeBecomingTrue()
    }

    fun becomingFalse(): OnEveryTimeBeingX {
        return every().single().timeBecomingFalse()
    }
}

class OnTheNth(val condition: () -> Boolean, val n: Int) {
    fun timeBeingTrue(): OnImpl {
        return timeBecomingX(condition, target = true)
    }

    fun timeBeingFalse(): OnImpl {
        return timeBecomingX(condition, target = false)
    }

    fun timeBecomingTrue(): OnImpl {
        val sed = SignalEdgeDetector(condition)
        return timeBecomingX(sed::risingEdge, target = true)
    }

    fun timeBecomingFalse(): OnImpl {
        val sed = SignalEdgeDetector(condition)
        return timeBecomingX(sed::fallingEdge, target = false)
    }

    private fun timeBecomingX(condition: () -> Boolean, target: Boolean): OnImpl {
        var hasBeenCalledBefore = false

        val actualCondition = {
            hasBeenCalledBefore = true
            condition() == target
        }
        
        return OnImpl(actualCondition, n) {
            hasBeenCalledBefore
        }
    }
}

fun main() {
    var calls = -1

    On { calls++; calls in 1..3 || calls in 10..12 }.every().single().timeBecomingTrue().extendFor(100).milliseconds().until { calls >= 100 }
        .execute { println("hi2 ($calls)") }

    Scheduler.launchManually({ true })
}

class OnImpl(
    val condition: () -> Boolean,
    val requiredTrueStreak: Int,
    val extendCondition: (Boolean, Long) -> Boolean = { _, _ -> false },
    val untilCondition: (Long) -> Boolean = { false },
) : Schedulable {
    init {
        hook()
    }

    private var totalCalls = 0L
    private var trueStreak = 0

    private var canBeExtended = false

    private var actions = mutableSetOf<Runnable>()
    
    fun execute(action: Runnable) {
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
