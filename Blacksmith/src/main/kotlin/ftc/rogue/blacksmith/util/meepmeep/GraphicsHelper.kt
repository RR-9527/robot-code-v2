package ftc.rogue.blacksmith.util.meepmeep

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorPalette
import java.lang.reflect.Method

class GraphicsHelper(mm: MeepMeep) {
    private val g: Any

    private val drawString: Method
    private val setFont: Method
    private val setColor: Method

    init {
        val c = mm.canvas
        val b = c::class.java.getMethod("getBufferStrat").invoke(c)

        g = b::class.java.getMethod("getDrawGraphics").invoke(b)!!

        drawString = g::class.java.getMethod("drawString", String::class.java, Int::class.java, Int::class.java)

        setFont  = g::class.java.methods.first { it.name == "setFont"  }
        setColor = g::class.java.methods.first { it.name == "setColor" }
    }

    fun setFont(font: Any?) {
        setFont.invoke(g, font)
    }

    fun setColor(color: Any?) {
        setColor.invoke(g, color)
    }

    fun drawString(str: String, x: Int, y: Int) {
        drawString.invoke(g, str, x, y)
    }

    companion object {
        fun newFont(name: String, style: Int, size: Int) =
            Class.forName("java.awt.Font")
                .getConstructor(String::class.java, Int::class.java, Int::class.java)
                .newInstance(name, style, size);

        fun getColor(color: String) =
            ColorPalette::class.java
                .getMethod("get${color.uppercase()}")
                .invoke(ColorManager.COLOR_PALETTE)
    }
}
