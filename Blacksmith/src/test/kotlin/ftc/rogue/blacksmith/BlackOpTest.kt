package ftc.rogue.blacksmith

import com.qualcomm.robotcore.hardware.HardwareMap
import ftc.rogue.blacksmith.annotations.CreateOnGo
import ftc.rogue.blacksmith.internal.blackop.injectCreateOnGoFields
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

    data class Teast(val hwMap: HardwareMap? = null)
}
