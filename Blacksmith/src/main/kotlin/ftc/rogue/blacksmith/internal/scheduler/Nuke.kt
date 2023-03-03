@file:JvmName("Nuke")

package ftc.rogue.blacksmith.internal.scheduler

const val Listeners = 0x1
const val Messages = 0x2
const val BeforeEach = 0x4
const val All = Listeners or Messages or BeforeEach
