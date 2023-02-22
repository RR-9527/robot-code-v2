package ftc.rogue.blacksmith.listeners

/**
 * [Docs link](https://blacksmithftc.vercel.app/scheduler-api/pulsar)
 */
class Pulsar(interval: Int) {
    private var startTime = System.currentTimeMillis()

    private val listener = Listener { System.currentTimeMillis() > startTime + interval }
        .onRise { startTime = System.currentTimeMillis() }

    fun onPulse(callback: Runnable) = apply {
        listener.onRise(callback)
    }
}
