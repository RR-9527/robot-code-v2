package ftc.rogue.blacksmith.listeners

import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.internal.scheduler.OnEvery
import ftc.rogue.blacksmith.internal.scheduler.OnEveryTimeBeingX

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
// | --- other()  |
// | --- third()  |
// | --- nth(n)   :
// | ----- timeBeingTrue()     |
// | ----- timeBeingFalse()    |
// | ----- timeBecomingTrue()  |
// | ----- timeBecomingFalse() :
// | ------ withOffset() ^
// | ------- doUntil(λn.b)
// | ------- doFor(n):
// | --------- iterations()
// | --------- milliseconds()
// | --------- time(u)
// | ------- doWhile(λn.b)
// | - beingTrue():
// | --> every().single().timeBeingTrue()
// | - beingFalse():
// | --> every().single().timeBeingFalse()
// | - becomingTrue():
// | --> every().single().timeBecomingTrue()
// | - becomingFalse():
// | --> every().single().timeBecomingFalse()
// |
// | Terminal operations:
// | - until()
// | - forever()
// |
// | Finally:
// | - execute(λ)
// --------------------------------
class On(val condition: () -> Boolean) {
//    fun theFirst(): OnTheNth {
//        return OnTheNth(condition, n = 1)
//    }
//
//    fun theSecond(): OnTheNth {
//        return OnTheNth(condition, n = 2)
//    }
//
//    fun theThird(): OnTheNth {
//        return OnTheNth(condition, n = 3)
//    }
//
//    fun theNth(n: Int): OnTheNth {
//        return OnTheNth(condition, n)
//    }

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

    On { calls in 1..3 || calls in 10..12 }
        .every()
        .single()
        .timeBeingTrue()
        .withOffset(2)
//        .extendFor(3)
//        .iterations()
        .doUntil { _, _ -> false }
        .forever()
        .execute { println("hi2 ($calls)") }

    Scheduler.launchManually({ calls++ < 16 })
}
