@file:Suppress("UNCHECKED_CAST")

package ftc.rogue.blacksmith.internal.util

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

private fun Any.verifyThatImNotBeingADumbAss() {
    if (this::class.simpleName == "Class") {
        throw RuntimeException("You're calling this on a Class<*> by accident, dumbass")
    }
}

// TODO: abstract some of this stuff into private methods. I'll do it later :)
internal fun Any.getMethod(name: String, vararg params: Class<*>): Method {
    verifyThatImNotBeingADumbAss()
    return this::class.java.getMethod(name, *params)
}

internal fun Any.getDeclaredMethod(name: String, vararg params: Class<*>): Method {
    verifyThatImNotBeingADumbAss()
    return this::class.java.getDeclaredMethod(name, *params)
}

internal fun Any.getMethodI(name: String, vararg params: Any): Method {
    verifyThatImNotBeingADumbAss()
    val paramTypes = params
        .map { it::class.java }
        .toTypedArray()

    return this::class.java.getMethod(name, *paramTypes)
}

internal fun <T> Any.invokeMethod(name: String, vararg params: Pair<Any, Class<*>>): T {
    verifyThatImNotBeingADumbAss()

    val paramTypes = params
        .map { it.second }
        .toTypedArray()

    val paramVals = params
        .map { it.first }
        .toTypedArray()

    return this.getMethod(name, *paramTypes).invoke(this, *paramVals) as T
}

internal fun <T> Any.invokeMethodI(name: String, vararg params: Any): T {
    verifyThatImNotBeingADumbAss()

    val paramTypes = params
        .map { it to it::class.java }
        .toTypedArray()

    return this.invokeMethod(name, *paramTypes) as T
}

internal fun <T> Any.invokeMethodRethrowing(name: String, vararg params: Pair<Any, Class<*>>): T {
    verifyThatImNotBeingADumbAss()

    try {
        return this.invokeMethod(name, *params)
    } catch (e: InvocationTargetException) {
        throw e.targetException
    }
}

internal fun <T> Any.invokeMethodRethrowingI(name: String, vararg params: Any): T {
    verifyThatImNotBeingADumbAss()

    try {
        return this.invokeMethodI(name, *params)
    } catch (e: InvocationTargetException) {
        throw e.targetException
    }
}

fun Class<*>.getDeclaredFieldsAnnotatedWith(annotation: Class<out Annotation>): List<Field> {
    return this.declaredFields
        .filter {
            it.isAnnotationPresent(annotation)
        }
}
