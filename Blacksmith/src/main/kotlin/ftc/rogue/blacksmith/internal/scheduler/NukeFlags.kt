@file:JvmName("Nuke")

package ftc.rogue.blacksmith.internal.scheduler

data class NukeFlag internal constructor(val flag: Int) {
    @JvmSynthetic
    internal infix fun and(other: NukeFlag): NukeFlag {
        return NukeFlag(flag and other.flag)
    }

    @JvmSynthetic
    internal infix fun or(other: NukeFlag): NukeFlag {
        return NukeFlag(flag or other.flag)
    }

    @JvmSynthetic
    internal operator fun not(): NukeFlag {
        return NukeFlag(flag.inv())
    }
}

@JvmField
val Schedulables = NukeFlag(0x1)

@JvmField
val Messages = NukeFlag(0x2)

@JvmField
val BeforeEach = NukeFlag(0x4)

@JvmField
val All = Schedulables or Messages or BeforeEach
