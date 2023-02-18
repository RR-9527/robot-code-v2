@file:Suppress("FunctionName")

package ftc.rogue.blacksmith.internal.blackop

import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.annotations.CreateOnGo
import ftc.rogue.blacksmith.internal.util.getFieldsAnnotatedWith
import kotlin.reflect.KProperty

@PublishedApi
internal class CreationException(message: String) : RuntimeException(message)

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

    var vale = "s"

    init {
        Scheduler.on(BlackOp.STARTING_MSG) {
            vale = "inti"
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

@JvmSynthetic
internal fun Any.injectCreateOnGoFields() = this::class.java
    .getFieldsAnnotatedWith(CreateOnGo::class.java)
    .forEach { field ->
        val clazz = field.type

        val shouldPassHwMap = field.getAnnotation(CreateOnGo::class.java)?.passHwMap == true

        val instance = if (shouldPassHwMap) {
            clazz.getConstructor().newInstance(BlackOp.hwMap)
        } else {
            clazz.getConstructor().newInstance()
        }

        field.isAccessible = true
        field.set(this, instance)
    }
