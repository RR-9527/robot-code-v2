//package ftc.rogue.blacksmith.internal.scheduler
//
//import ftc.rogue.blacksmith.listeners.OnEveryImpl
//import ftc.rogue.blacksmith.units.TimeUnit
//import ftc.rogue.blacksmith.util.SignalEdgeDetector
//
//class OnTheNth(
//    val condition: () -> Boolean,
//    val n: Int,
//) {
//    fun timeBeingTrue(): OnTheNthTimeBeingX {
//        return OnTheNthTimeBeingX(condition, n)
//    }
//
//    fun timeBeingFalse(): OnTheNthTimeBeingX {
//        return OnTheNthTimeBeingX({ !condition() }, n)
//    }
//
//    fun timeBecomingTrue(): OnTheNthTimeBeingX {
//        val sed = SignalEdgeDetector(condition)
//        return OnTheNthTimeBeingX({ sed.update(); sed.risingEdge() }, n)
//    }
//
//    fun timeBecomingFalse(): OnTheNthTimeBeingX {
//        val sed = SignalEdgeDetector(condition)
//        return OnTheNthTimeBeingX({ sed.update(); sed.fallingEdge() }, n)
//    }
//}
//
//class OnTheNthTimeBeingX(
//    val condition: () -> Boolean,
//    val n: Int,
//) {
//    fun extendFor(x: Int): OnTheNthTimeBeingXDoYFor {
//        return OnTheNthTimeBeingXDoYFor(condition, n, x)
//    }
//
//    fun doUntil(extendCondition: (Boolean, Long) -> Boolean): OnTheNthTimeBeingXDoExtraIterationsUntil {
//        return OnTheNthTimeBeingXDoExtraIterationsUntil(condition, n, extendCondition)
//    }
//
//    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
//        return OnEveryImpl(condition, n, untilCondition = untilCondition, nthTime = 1)
//    }
//
//    fun forever(): OnEveryImpl {
//        return until { false }
//    }
//}
//
//class OnTheNthTimeBeingXDoYFor(
//    val condition: () -> Boolean,
//    val n: Int,
//    val x: Int,
//) {
//    fun iterations(): OnTheNthTimeBeingXDoYForZExtraIterations {
//        if (n < 0) {
//            throw IllegalArgumentException("Can't extend for negative ($x) iterations")
//        }
//
//        return OnTheNthTimeBeingXDoYForZExtraIterations(condition, n, x)
//    }
//
//    fun milliseconds(): OnTheNthTimeBeingXDoYForZTime {
//        if (n < 0) {
//            throw IllegalArgumentException("Can't extend for negative ($x) time")
//        }
//
//        return time(TimeUnit.MILLISECONDS)
//    }
//
//    fun seconds(): OnTheNthTimeBeingXDoYForZTime {
//        if (n < 0) {
//            throw IllegalArgumentException("Can't extend for negative ($x) time")
//        }
//
//        return time(TimeUnit.SECONDS)
//    }
//
//    fun time(unit: TimeUnit): OnTheNthTimeBeingXDoYForZTime {
//        return OnTheNthTimeBeingXDoYForZTime(condition, n, unit.toMs(x))
//    }
//}
//
//class OnTheNthTimeBeingXDoYForZExtraIterations(
//    val condition: () -> Boolean,
//    val n: Int,
//    val iterations: Int,
//) {
//    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
//        var offset = 0L
//        var condWasTrue = true
//
//        val extendCondition = { condIsTrue: Boolean, curr: Long ->
//            if (!condIsTrue && condWasTrue) {
//                offset = curr
//            }
//
//            condWasTrue = condIsTrue
//            curr - offset < iterations
//        }
//
//        return OnEveryImpl(condition, n, extendCondition, nthTime = 1, untilCondition)
//    }
//
//    fun forever(): OnEveryImpl {
//        return until { false }
//    }
//}
//
//class OnTheNthTimeBeingXDoYForZTime(
//    val condition: () -> Boolean,
//    val n: Int,
//    val ms: Int,
//) {
//    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
//        var offset = 0L
//        var condWasTrue = true
//
//        val extendCondition = { condIsTrue: Boolean, _: Long ->
//            if (!condIsTrue && condWasTrue) {
//                offset = System.currentTimeMillis()
//            }
//
//            condWasTrue = condIsTrue
//            System.currentTimeMillis() - offset < ms
//        }
//
//        return OnEveryImpl(condition, n, extendCondition, nthTime = 1, untilCondition)
//    }
//
//    fun forever(): OnEveryImpl {
//        return until { false }
//    }
//}
//
//class OnTheNthTimeBeingXDoExtraIterationsUntil(
//    val condition: () -> Boolean,
//    val n: Int,
//    val extendCondition: (Boolean, Long) -> Boolean,
//) {
//    fun until(untilCondition: (Long) -> Boolean): OnEveryImpl {
//        return OnEveryImpl(condition, n, { b, l -> !extendCondition(b, l) }, nthTime = 1, untilCondition)
//    }
//
//    fun forever(): OnEveryImpl {
//        return until { false }
//    }
//}
