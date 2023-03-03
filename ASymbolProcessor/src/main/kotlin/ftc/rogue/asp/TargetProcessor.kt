@file:Suppress("SpellCheckingInspection")

package ftc.rogue.asp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import ftc.rogue.asp.annotations.Component
import ftc.rogue.asp.etc.GenerationException
import ftc.rogue.asp.models.Clazz
import ftc.rogue.asp.models.Var
import java.io.File

class TargetProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        val classes = resolver.getHwComponentClasses()

        if (!classes.iterator().hasNext())
            return emptyList()

        try {
            classes.forEach { clazz ->
                generateFile(clazz)
            }
        } catch (e: GenerationException) {
            env.logger.error(e.localizedMessage)
        }

        invoked = true

        return classes
            .filterNot { it.validate() }
            .toList()
    }

    private fun generateFile(_clazz: KSClassDeclaration) {
        val clazz = Clazz.from(_clazz)
        val valsSB = StringBuilder()
        val methodsSB = StringBuilder()

        clazz.vars
            .filterNotNull()
            .forEach { value ->
                processVal(value, clazz, valsSB, methodsSB)
            }

        val outFile = env.codeGenerator.createNewFile(
            Dependencies(aggregating = false, clazz.file),
            clazz.pakig,
            clazz.fileName,
        )

        val lines = File(clazz.file.filePath)
            .bufferedReader()
            .readLines()

        var currentLine = -1

        val imports = buildString {
            while ("object" !in lines[++currentLine] && currentLine < lines.size) {
                val trimmedLine = lines[currentLine].trimStart()

                if (trimmedLine.startsWith("@Com"))
                    continue

                if (trimmedLine.startsWith("//"))
                    continue

                append(lines[currentLine])
                append("\n")
            }
            delete(length - 1, length)
        }

        currentLine -= 1

        val clazzString = buildString {
            while (++currentLine < lines.size) {
                val trimmedLine = lines[currentLine].trimStart()

                if (trimmedLine.startsWith("const val"))
                    continue

                if (trimmedLine.startsWith("@Set"))
                    continue

                if (trimmedLine.startsWith("//"))
                    continue

                append(lines[currentLine])
                append("\n")
            }
        }

        val oldClassHeader = "object ${clazz.simpleName} {"
        val newClassHeader = "class ${clazz.newName} {"

        val clazzStart = clazzString
            .substringBeforeLast("}")
            .replace(oldClassHeader, newClassHeader)
            .let {
                newClassHeader + "\n\t" + it.substringAfter(newClassHeader).trimStart(' ', '\n')
            }

        val clazzEnd = "}\n"

        valsSB.append("\n")

        methodsSB.delete(methodsSB.length - 1, methodsSB.length)

        val atConfig = "@file:com.acmerobotics.dashboard.config.Config\n\n"

        val outString =
            atConfig   +
            imports    +
            valsSB     +
            clazzStart +
            methodsSB  +
            clazzEnd

        outFile.write(outString.toByteArray())
    }

    private fun processVal(
        value: Var,
        clazz: Clazz,
        valsSB: StringBuilder,
        methodsSB: StringBuilder
    ) {
        valsSB.append("""
            
            @JvmField
            var ${value.name} = ${value.value}
            
            """.trimIndent())

        if (!value.isTarget)
            return

        val methodName = targetVarToMethodName(clazz, value)
        val targetName = clazz.annot.targetProperty

        methodsSB.append("""
            
            fun $methodName() {
                $targetName = ${value.name}
            }
            
            """.replaceIndent("\t"))
    }

    private fun targetVarToMethodName(clazz: Clazz, value: Var): String {
        val methodFormat = clazz.annot.methodFormat
        val removeClassNameFromMethodName = clazz.annot.removeClassNameFromMethodName

        val customMethodName = value.methodName

        if (customMethodName != null) {
            return customMethodName
        }

        val newVarName =
            if (removeClassNameFromMethodName) {
                value.name.replace(clazz.newName, "", ignoreCase = true)
            } else {
                value.name
            }

        return methodFormat
            .replace("*", newVarName)
            .replaceFirstChar(Char::lowercase)
    }

    private fun Resolver.getHwComponentClasses(): Sequence<KSClassDeclaration> {
        val clazzName = Component::class.qualifiedName!!
        val symbols = getSymbolsWithAnnotation(clazzName)
        return symbols.filterIsInstance<KSClassDeclaration>()
    }
}
