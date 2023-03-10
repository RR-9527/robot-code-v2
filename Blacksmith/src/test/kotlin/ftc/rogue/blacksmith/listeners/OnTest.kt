package ftc.rogue.blacksmith.listeners

import ftc.rogue.blacksmith.Scheduler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnTest {
    @Test
    fun `on-every() test #1`() {
        var calls = 0
        val nums = mutableListOf<Int>()

        On { calls in 1..3 || calls in 10..12 }.every().single().timeBecomingTrue().extendFor(3).iterations().until { calls > 12 }
            .execute { nums += calls }

        Scheduler.launchManually({ calls++ < 16 })

        assertTrue { nums == listOf(1, 2, 3, 4, 10, 11, 12) }
    }

    @Test
    fun `on-every() test #2`() {
        var calls = 0
        val nums = mutableListOf<Int>()

        On { calls in 1..3 || calls in 10..12 }.every().other().timeBeingFalse().until { calls > 14 }
            .execute { nums += calls }

        Scheduler.launchManually({ calls++ < 16 })

        assertTrue { nums == listOf(5, 7, 9, 14) }
    }

    @Test
    fun `on-every() test #3`() {
        var calls = 0
        val nums = mutableListOf<Int>()

        On { calls in 1..3 || calls in 10..12 }.every().nth(5).timeBecomingFalse().forever()
            .execute { nums += calls }

        Scheduler.launchManually({ calls++ < 16 })

        assertTrue { nums == emptyList<Int>() }
    }

    @Test
    fun `on-every() test #4`() {
        var calls = 0
        val nums = mutableListOf<Int>()

        On { calls in 1..3 || calls in 10..12 }.every().other().timeBecomingFalse().doUntil { _, c -> c > 14 }.forever()
            .execute { nums += calls }

        Scheduler.launchManually({ calls++ < 16 })

        assertTrue { nums == listOf(13, 14) }
    }
}
