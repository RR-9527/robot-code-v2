@file:Suppress("SpellCheckingInspection")

package ftc.rogue.asp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

class TargetProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val files = resolver.findAnnotations(env, HwComponentFile::class)

        val fileVals = files.toList().map { file ->
            file to file
                .declarations
                .filterIsInstance<KSPropertyDeclaration>()
                .toList()
        }.toMap()

        if (fileVals.isEmpty())
            return emptyList()

        fileVals.forEach { (file, vals) ->
            generateFile(env, file, vals)
        }

        return fileVals
            .keys
            .filterNot { it.validate() }
            .toList()
    }

    private fun generateFile(
        env: SymbolProcessorEnvironment,
        file: KSFile,
        vals: List<KSPropertyDeclaration>
    ) {
        val pakage = file.packageName.asString()
        val fileName = file.fileName.substringAfterLast(".") + "Generated"

        val fileText = buildString {
            append("""
                @file:com.acmerobotics.dashboard.config.Config

                package $pakage

            """.trimIndent())

            vals.forEach { value ->
                val simpleName = value.simpleName.asString()

                val methodName = "goTo" + simpleName
                    .lowercase()
                    .substringAfter("t_")
                    .replace("_(\\w)".toRegex()) {
                        it.groupValues[1].uppercase()
                    }
                    .capitalize()

                if (simpleName.startsWith("t_")) {
                    append("""

                    @JvmField var ${simpleName.substringAfter("t_")} = ${value.qualifiedName?.asString()}
                    
                    // $methodName

                    """.trimIndent())
                }
            }
        }

        val sourceFiles = vals.mapNotNull { it.containingFile }

        val outFile = this.env.codeGenerator.createNewFile(
            Dependencies(
                aggregating = false,
                *sourceFiles.toList().toTypedArray(),
            ),
            pakage,
            fileName,
        )

        outFile.write(fileText.toByteArray())
    }

    private fun Resolver.findAnnotations(env: SymbolProcessorEnvironment, kClass: KClass<*>): Sequence<KSFile> {
        val clazzName = kClass.qualifiedName!!

        val symbols = getSymbolsWithAnnotation(clazzName)

        return symbols.filterIsInstance<KSClassDeclaration>()
            .map { it.containingFile!! }
    }
}
