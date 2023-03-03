package ftc.rogue.asp.models

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import ftc.rogue.asp.annotations.Component

class Clazz(
    val newName: String,
    val simpleName: String,
    val pakig: String,
    val fileName: String,
    val vars: List<Var?>,
    val annot: Component,
    val file: KSFile,
) {
    companion object {
        @OptIn(KspExperimental::class)
        fun from(clazz: KSClassDeclaration) = Clazz(
            newName    = clazz.simpleName.asString().replace("Component", ""),
            simpleName = clazz.simpleName.asString(),
            pakig      = clazz.packageName.asString(),
            file       = clazz.containingFile!!,
            fileName   = clazz.simpleName.asString().replace("Component", "") + "Generated",
            annot      = clazz.getAnnotationsByType(Component::class).first(),
            vars       = clazz.getAllProperties().toList().map(Var::from)
        )
    }
}
