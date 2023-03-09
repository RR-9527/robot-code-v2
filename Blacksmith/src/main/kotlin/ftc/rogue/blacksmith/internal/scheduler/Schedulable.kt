package ftc.rogue.blacksmith.internal.scheduler

import ftc.rogue.blacksmith.Scheduler

interface Schedulable {
    fun tick()
    fun destroy()

    fun hook() {
        Scheduler.hook(this)
    }
}
