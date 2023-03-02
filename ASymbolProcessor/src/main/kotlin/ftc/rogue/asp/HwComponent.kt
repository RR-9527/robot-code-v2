package ftc.rogue.asp

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HwComponent(
    val targetProperty: String,
    val methodFormat: String = "goTo*",
    val removeClassNameFromMethodName: Boolean = true,
)
