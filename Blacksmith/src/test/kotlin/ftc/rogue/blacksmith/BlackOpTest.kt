package ftc.rogue.blacksmith

import com.qualcomm.robotcore.hardware.HardwareMap
import ftc.rogue.blacksmith.annotations.CreateOnGo
import ftc.rogue.blacksmith.annotations.EvalOnGo
import ftc.rogue.blacksmith.internal.blackop.CreationException
import ftc.rogue.blacksmith.internal.blackop.injectCreateOnGoFields
import ftc.rogue.blacksmith.internal.blackop.injectEvalOnGoFields
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue

class BlackOpTest {
    @JvmField
    @CreateOnGo
    var hi1: Teast? = null

    @JvmField
    @CreateOnGo(passHwMap = true)
    var hi2: Teast? = null

    @Test
    fun `@CreateOnGo concept works`() {
        // Should throw IllegalStateException since BlackOp.hwMap not initialized yet
        assertThrows<IllegalStateException> {
            injectCreateOnGoFields()
        }

        assertTrue { hi1 == Teast() }
    }

    data class Teast(val hwMap: HardwareMap? = null) {
        companion object {
            @JvmStatic
            fun getTeacH(): Teast {
                return Teast()
            }
        }
    }

    @JvmField
    @EvalOnGo("getTeast")
    var bye1: Teast? = null

    @JvmField
    @EvalOnGo("getTeacH", clazz = Teast::class)
    var bye2: Teast? = null

    @JvmField
    @EvalOnGo("getNotTeast")
    var bye3: Teast? = null

    @Test
    fun `@EvalOnGo concept works`() {
        // Should throw IllegalArgumentException since it's trying to assign a String to a Teast
        assertThrows<CreationException> {
            injectEvalOnGoFields()
        }

        assertTrue("bye1") { bye1 == Teast() }
        assertTrue("bye2") { bye2 == Teast() }
        assertTrue("bye3") { bye3 == null }
    }

    private fun getTeast(): Teast {
        return Teast()
    }

    private fun getNotTeast(): String {
        return "nope"
    }
}