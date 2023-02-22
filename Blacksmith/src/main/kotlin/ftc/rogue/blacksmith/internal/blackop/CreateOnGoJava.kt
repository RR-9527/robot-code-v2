package ftc.rogue.blacksmith.internal.blackop

import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.annotations.CreateOnGo
import ftc.rogue.blacksmith.annotations.EvalOnGo
import ftc.rogue.blacksmith.internal.util.getDeclaredFieldsAnnotatedWith
import ftc.rogue.blacksmith.internal.util.getDeclaredMethod
import java.lang.reflect.Method

@JvmSynthetic
internal fun Any.injectCreateOnGoFields() = this::class.java
    .getDeclaredFieldsAnnotatedWith(CreateOnGo::class.java)
    .forEach { field ->
        val clazz = field.type

        val shouldPassHwMap = field.getAnnotation(CreateOnGo::class.java)?.passHwMap == true

        try {

            val instance = if (shouldPassHwMap) {
                clazz.getConstructor().newInstance(BlackOp.hwMap)
            } else {
                clazz.getConstructor().newInstance()
            }

            field.isAccessible = true
            field.set(this, instance)
        } catch(e: NoSuchMethodException) {
            val errorMsg = if (shouldPassHwMap) {
                "No constructor found that takes only a hardwareMap"
            } else {
                "No no-args constructor found"
            }

            throw CreationException(errorMsg)
        }
    }

@JvmSynthetic
internal fun Any.injectEvalOnGoFields() = this::class.java
    .getDeclaredFieldsAnnotatedWith(EvalOnGo::class.java)
    .forEach { field ->
        val methodName  = field.getAnnotation(EvalOnGo::class.java)!!.method
        val methodClazz = field.getAnnotation(EvalOnGo::class.java)!!.clazz

        lateinit var method: Method

        try {
            method = if (methodClazz == EvalOnGo.ClassThatTheAnnotationIsIn::class) {
                this::class.java.getDeclaredMethod(methodName)
            } else {
                methodClazz.java.getDeclaredMethod(methodName)
            }

            val returnValue = method.let {
                it.isAccessible = true
                it.invoke(this)
            }

            field.isAccessible = true
            field.set(this, returnValue)
        } catch (e: NoSuchMethodException) {
            throw CreationException("No method $methodName() exists for @EvalOnGo. Are you sure there's a version with no args?")
        } catch (e: IllegalArgumentException) {
            throw CreationException("""
                $methodName() doesn't return the correct type for '${field.name}' (for @EvalOnGo)
                 - Expected: ${field.type.name}
                 - Actual: ${method.returnType.name}
            """.trimIndent())
        }
    }
