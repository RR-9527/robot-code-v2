package ftc.rogue.blacksmith.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EvalOnGo(
    val method: String,
    val clazz: KClass<*> = ClassThatTheAnnotationIsIn::class,
) {
    object ClassThatTheAnnotationIsIn
}
