package ftc.rogue.blacksmith.annotations

import ftc.rogue.blacksmith.internal.blackop.ClassThatTheAnnotationIsIn
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EvalOnGo(
    val method: String,
    val clazz: KClass<*> = ClassThatTheAnnotationIsIn::class,
)
