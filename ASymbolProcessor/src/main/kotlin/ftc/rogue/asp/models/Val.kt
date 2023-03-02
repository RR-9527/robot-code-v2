package ftc.rogue.asp.models

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import ftc.rogue.asp.annotations.SetMethodName

class Val(
    val name: String,
    val value: String,
    val isTarget: Boolean,
    val methodName: String?,
) {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(value: KSPropertyDeclaration): Val? {
            val fullName = value.simpleName.asString()

            if (!fullName.isTargetVariable && !fullName.isConfigVariable)
                return null

            return Val(
                name = fullName.substring(1),
                value = value.qualifiedName!!.asString(),
                isTarget = fullName.isTargetVariable,
                methodName = value
                    .getAnnotationsByType(SetMethodName::class)
                    .firstOrNull()
                    ?.value,
            )
        }

        private val String.isTargetVariable
            get() = length >= 2 && startsWith("t") && this[1].isUpperCase()

        private val String.isConfigVariable
            get() = length >= 2 && startsWith("c") && this[1].isUpperCase()
    }
}
