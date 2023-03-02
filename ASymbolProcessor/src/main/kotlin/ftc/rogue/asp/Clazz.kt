package ftc.rogue.asp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

class Clazz(
    val newName: String,
    val simpleName: String,
    val pakig: String,
    val fileName: String,
    val vals: List<KSPropertyDeclaration>,
    val annot: HwComponent,
    val file: KSFile,
) {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(clazz: KSClassDeclaration) = Clazz(
            newName    = clazz.simpleName.asString().replace("Component", ""),
            simpleName = clazz.simpleName.asString(),
            pakig      = clazz.packageName.asString(),
            file       = clazz.containingFile!!,
            annot      = clazz.getAnnotationsByType(HwComponent::class).first(),
            fileName   = clazz.containingFile!!.fileName.substringBeforeLast(".").substringAfterLast(".") + "Generated",
            vals = clazz
                .declarations
                .filterIsInstance<KSPropertyDeclaration>()
                .toList()
        )
    }
}
