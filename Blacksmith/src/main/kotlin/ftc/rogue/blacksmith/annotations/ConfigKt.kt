@file:Suppress("IllegalIdentifier")

package ftc.rogue.blacksmith.annotations

/* Doesn't really work yet lol */

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigKt (
    val value: String = ""
)
