package ftc.rogue.blacksmith.annotations

/**
 * [Link to docs (please read b4 using)](https://blacksmithftc.vercel.app/black-op/create-on-go-java)
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateOnGo(
    val passHwMap: Boolean = false
)
