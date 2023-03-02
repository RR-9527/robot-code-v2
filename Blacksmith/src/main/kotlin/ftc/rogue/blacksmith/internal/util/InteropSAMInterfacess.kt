package ftc.rogue.blacksmith.internal.util

import com.qualcomm.robotcore.util.ElapsedTime
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.internal.anvil.AnvilRunConfig

typealias MotionModel = (Double, ElapsedTime) -> Double

fun interface AnvilRunConfigBuilder {
    fun AnvilRunConfig.build()
}

typealias AnvilConsumer = Consumer<Anvil>

fun interface Consumer<T> {
    fun T.consume()
}

@JvmSynthetic
fun <T> Consumer<T>.consume(scope: T) {
    scope.consume()
}

fun interface AnvilCycle {
    fun Anvil.doCycle(iteration: Int)
}

@JvmSynthetic
fun AnvilCycle.consume(instance: Anvil, iteration: Int) {
    instance.doCycle(iteration)
}
