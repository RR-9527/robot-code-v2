package ftc.rogue.blacksmith.listeners

import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.internal.scheduler.OnEvery
import ftc.rogue.blacksmith.internal.scheduler.OnEveryTimeBeingX
import ftc.rogue.blacksmith.internal.scheduler.OnTheNth
import ftc.rogue.blacksmith.internal.scheduler.Schedulable

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

fun main() {
    var calls = 0

    On { calls in 1..3 || calls in 10..12 }.theFirst().timeBeingFalse().extendFor(3).iterations().forever()
        .execute { println("hi2 ($calls)") }

    Scheduler.launchManually({ calls++ < 16 })
}

class OnImpl(
    val condition: () -> Boolean,
    val requiredTrueStreak: Int,
    val extendCondition: (Boolean, Long) -> Boolean = { _, _ -> false },
    val nthTime: Long = Long.MAX_VALUE,
    val untilCondition: (Long) -> Boolean = { false },
) : Schedulable {
    init {
        hook()
    }

    private var totalCalls = 0L
    private var trueStreak = 0
    private var numTrueConds = 0L

    private var canBeExtended = false
    private var lastChanceToLookAtMeHector = false

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
            numTrueConds++
        }

        val canExtendAction = extendCondition(conditionIsTrue && trueStreak >= requiredTrueStreak, totalCalls)
        val shouldExtendAction = canBeExtended && canExtendAction

        if (trueStreak < requiredTrueStreak && !shouldExtendAction) {
            canBeExtended = false
            return
        }

        if (!canExtendAction) {
            lastChanceToLookAtMeHector = false
        }

        if (numTrueConds == nthTime) {
            lastChanceToLookAtMeHector = true
        }

        if (numTrueConds > nthTime && !lastChanceToLookAtMeHector) {
            destroy()
            return
        }

        if (conditionIsTrue && !shouldExtendAction) {
            trueStreak = 0
        }

        if ((conditionIsTrue || shouldExtendAction) && lastChanceToLookAtMeHector) {
            canBeExtended = true
            actions.forEach(Runnable::run)
        }
    }

    override fun destroy() {
        actions.clear()
        Scheduler.unhook(this)
    }
}
