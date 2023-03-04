package ftc.rogue.blacksmith

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import ftc.rogue.blacksmith.internal.scheduler.Listeners
import ftc.rogue.blacksmith.internal.scheduler.Messages
import ftc.rogue.blacksmith.internal.scheduler.NukeFlag
import ftc.rogue.blacksmith.listeners.Listener
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SchedulerTest {
    private val linearOpMode = mockk<LinearOpMode>()
    private var isStopped = false

    init {
        every { linearOpMode.opModeIsActive() } answers { !isStopped }
        every { linearOpMode.isStopRequested } answers { isStopped }
    }

    @BeforeEach
    fun setUp() {
        Scheduler.nuke()
        isStopped = false
    }

    @Test
    fun `scheduler properly runs everything and in the right order`() {
        val listeners = Array(3) { mockk<Listener>() }
        var output = ""

        listeners.forEachIndexed { index, listener ->
            every { listener.tick() } answers { output += index + 1 }
        }

        listeners.forEach(Scheduler::hookListener)

        Scheduler.beforeEach {
            output += "0"
        }

        Scheduler.launch(linearOpMode) {
            output += "4"
            isStopped = true
        }

        assertEquals("01234", output)
    }

    @Test
    fun `scheduler handles adding and deleting listeners from listener`() {
        val listeners = Array(4) { mockk<Listener>(relaxed = true) }

        for (i in 0..2) {
            Scheduler.hookListener(listeners[i])
        }

        assertDoesNotThrow {
            Scheduler.launch(linearOpMode) {
                Scheduler.unhookListener(listeners[1])
                Scheduler.hookListener(listeners[3])
                isStopped = true
            }
        }
    }

    @Test
    fun `scheduler messaging works`() {
        val msg = 234109324923L

        val expected = listOf("First", "Second")
        val actual = mutableListOf<String>()

        Scheduler.on(msg) {
            actual += "First"
        }

        Scheduler.on(msg) {
            actual += "Second"
        }

        Scheduler.on(0) {
            actual += "Should never be called"
        }

        Scheduler.emit(msg)

        assertEquals(expected, actual)
    }

    @Test
    fun `scheduler debug works (enough)`() {
        for (i in 0 until 3) {
            Listener { true }.hook()
        }

        for (i in 0 until 3) {
            Scheduler.on(0) {}
        }

        Scheduler.debug({ !isStopped }) {
            assertTrue("time > 0") { loopTime > 0 }
            assertTrue("numHookedListeners == 3") { numHookedListeners == 3 }
            assertTrue("numUniqueMessageSubs == 1") { numUniqueMessageSubs == 1 }
            isStopped = true
        }
    }

    @Test
    fun `scheduler nuke works (enough) #1`() {
        for (i in 0 until 3) {
            Listener { true }.hook()
        }

        Scheduler.on(0) {}

        Scheduler.nuke(Listeners, Messages)

        Scheduler.debug({ !isStopped }) {
            assertTrue("numHookedListeners == 0") { numHookedListeners == 0 }
            assertTrue("numUniqueMessageSubs == 0") { numUniqueMessageSubs == 0 }
            isStopped = true
        }
    }

    @Test
    fun `scheduler nuke works (enough) #2`() {
        for (i in 0 until 3) {
            Listener { true }.hook()
        }

        Scheduler.on(0) {}

        Scheduler.nuke()

        Scheduler.debug({ !isStopped }) {
            assertTrue("numHookedListeners == 0") { numHookedListeners == 0 }
            assertTrue("numUniqueMessageSubs == 0") { numUniqueMessageSubs == 0 }
            isStopped = true
        }
    }

    @Test
    fun `scheduler nuke works (enough) #3`() {
       assertThrows<IllegalArgumentException> {
           Scheduler.nuke(NukeFlag(29))
       }
    }
}
