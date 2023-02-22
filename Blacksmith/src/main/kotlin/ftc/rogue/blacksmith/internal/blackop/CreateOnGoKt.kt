@file:Suppress("FunctionName")

package ftc.rogue.blacksmith.internal.blackop

import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.Scheduler
import kotlin.reflect.KProperty

@PublishedApi
internal inline fun <reified T> CreateOnGoInternal(vararg args: () -> Any) =
    CreateOnGoInternal {
        val clazz = T::class.java

        val invokedArgs = args
            .map { it() }
            .toTypedArray()

        val argTypes = invokedArgs
            .map { it::class.java }
            .toTypedArray()

        val constructor = clazz.constructors
            .find { constructor ->
                constructor.parameterTypes contentEquals argTypes
            }

        constructor?.newInstance(*invokedArgs) as? T
            ?: throw CreationException("No constructor found for $clazz with args $argTypes")
    }

class CreateOnGoInternal<T : Any>
    @PublishedApi
    internal constructor(
        valueProvider: () -> T
    ) {

    private lateinit var value: T

    init {
        Scheduler.on(BlackOp.STARTING_MSG) {
            value = valueProvider()
        }
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (!::value.isInitialized) {
            throw IllegalStateException("createOnGo value is uninitialized (OpMode not started yet)")
        }
        return value
    }
}
