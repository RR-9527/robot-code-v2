package ftc.rogue.asp.models

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import ftc.rogue.asp.annotations.SetMethodName
import ftc.rogue.asp.etc.GenerationException

class Var(
    val name: String,
    val value: String,
    val isTarget: Boolean,
    val methodName: String?,
) {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(value: KSPropertyDeclaration): Var? {
            val fullName = value.simpleName.asString()

            if (!fullName.isTargetVariable && !fullName.isConfigVariable)
                return null

            return Var(
                name = fullName.substring(1),
                value = value.qualifiedName!!.asString(),
                isTarget = fullName.isTargetVariable,
                methodName = value
                    .getAnnotationsByType(SetMethodName::class)
                    .firstOrNull()
                    ?.value
                    ?.also {
                        validateIsNotSettingMethodNameToConfigVar(value)
                    },
            )
        }

        private val String.isTargetVariable
            get() = length >= 2 && startsWith("t") && this[1].isUpperCase()

        private val String.isConfigVariable
            get() = length >= 2 && startsWith("c") && this[1].isUpperCase()

        private fun validateIsNotSettingMethodNameToConfigVar(value: KSPropertyDeclaration) {
            val varName = value.simpleName.asString()

            if (varName.isTargetVariable)
                return

            val className = value.closestClassDeclaration()?.simpleName?.asString()
            val fullerVarName = (if (className != null) "$className." else "") + varName

            throw GenerationException("Can not set method name for a config variable ($fullerVarName)")
        }
    }
}
