@file:Suppress("SpellCheckingInspection")

package ftc.rogue.asp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import java.io.File
import kotlin.reflect.KClass

class TargetProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val classes = resolver.getHwComponentClasses()

        if (!classes.iterator().hasNext())
            return emptyList()

        classes.forEach { clazz ->
            generateFile(clazz)
        }

        return classes
            .filterNot { it.validate() }
            .toList()
    }

    private fun generateFile(_clazz: KSClassDeclaration) {
        val clazz = Clazz.from(_clazz)
        val valsSB = StringBuilder()
        val methodsSB = StringBuilder()

        clazz.vals.forEach { value ->
            processVal(value, clazz, valsSB, methodsSB)
        }

        val outFile = env.codeGenerator.createNewFile(
            Dependencies(
                aggregating = true,
                clazz.file,
            ),
            clazz.pakig,
            clazz.fileName,
        )

        val lines = File(clazz.file.filePath)
            .bufferedReader()
            .readLines()

        var currentLine = 0

        val imports = buildString {
            while ("@HwComponent" !in lines[currentLine] && currentLine < lines.size){
                append(lines[currentLine++])
                append("\n")
            }
            delete(length - 1, length)
        }

        val clazzString = buildString {
            while (++currentLine < lines.size) {
                if ("const val" in lines[currentLine])
                    continue

                append(lines[currentLine])
                append("\n")
            }
        }

        val newClazzHeader = "class ${clazz.newName} {"

        val (clazzStart, clazzEnd) = arrayOf(
            clazzString
                .substringBeforeLast("}")
                .replace("object ${clazz.simpleName} {", newClazzHeader)
                .let {
                    newClazzHeader + "\n\t" + it.substringAfter(newClazzHeader).trimStart(' ', '\n')
                },
            "}\n"
        )

        valsSB.delete(0, 1)
        valsSB.append("\n")

        methodsSB.delete(methodsSB.length - 1, methodsSB.length)

        val outString =
            imports    +
            valsSB     +
            clazzStart +
            methodsSB  +
            clazzEnd

        outFile.write(outString.toByteArray())
    }

    private fun processVal(
        value: KSPropertyDeclaration,
        clazz: Clazz,
        valsSB: StringBuilder,
        methodsSB: StringBuilder
    ) {
        val fullTargetName = value.simpleName.asString()

        if (!fullTargetName.isTargetVariable && !fullTargetName.isConfigVariable)
            return

        val targetName = fullTargetName.substring(1)

        valsSB.append("""
            
           @JvmField
           var $targetName = ${value.qualifiedName?.asString()}
           
           """.trimIndent())

        if (!fullTargetName.isTargetVariable)
            return

        val methodName = targetVarToMethodName(clazz, targetName)
        val targetProperty = clazz.annot.targetProperty

        methodsSB.append("""
            
                fun $methodName() {
                    $targetProperty = $targetName
                }
           
           """.replaceIndent("\t"))
    }

    private val String.isTargetVariable
        get() = length >= 2 && startsWith("t") && this[1].isUpperCase()

    private val String.isConfigVariable
        get() = length >= 2 && startsWith("c") && this[1].isUpperCase()

    private fun targetVarToMethodName(clazz: Clazz, varName: String): String {
        val methodFormat = clazz.annot.methodFormat
        val removeClassNameFromMethodName = clazz.annot.removeClassNameFromMethodName

        val newVarName =
            if (removeClassNameFromMethodName) {
                varName.replace(clazz.newName, "", ignoreCase = true)
            } else {
                varName
            }

        return methodFormat
            .replace("*", newVarName)
            .replaceFirstChar(Char::lowercase)
    }

    private fun Resolver.getHwComponentClasses(): Sequence<KSClassDeclaration> {
        val clazzName = HwComponent::class.qualifiedName!!
        val symbols = getSymbolsWithAnnotation(clazzName)
        return symbols.filterIsInstance<KSClassDeclaration>()
    }
}
