package ftc.rogue.asp.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Component(
    val targetProperty: String,
    val methodFormat: String = "goTo*",
    val removeClassNameFromMethodName: Boolean = true,
)
