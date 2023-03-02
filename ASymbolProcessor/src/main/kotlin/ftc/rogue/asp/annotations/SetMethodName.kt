package ftc.rogue.asp.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SetMethodName(
    val value: String,
)
