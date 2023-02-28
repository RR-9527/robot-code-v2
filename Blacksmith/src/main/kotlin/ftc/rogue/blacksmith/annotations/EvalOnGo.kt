package ftc.rogue.blacksmith.annotations

import ftc.rogue.blacksmith.internal.blackop.ClassThatTheAnnotationIsIn
import kotlin.reflect.KClass

/**
 * [Link to docs (please read b4 using)](https://blacksmithftc.vercel.app/black-op/eval-on-go-java)
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EvalOnGo(
    val method: String,
    val clazz: KClass<*> = ClassThatTheAnnotationIsIn::class,
)
