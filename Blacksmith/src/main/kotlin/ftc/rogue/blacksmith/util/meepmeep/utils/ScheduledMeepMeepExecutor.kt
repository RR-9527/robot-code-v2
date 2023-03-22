@file:Suppress("HasPlatformType")

package ftc.rogue.blacksmith.util.meepmeep.utils

import java.util.concurrent.Executors

internal object ScheduledMeepMeepExecutor {
    @get:JvmSynthetic
    val EXECUTOR = Executors.newSingleThreadScheduledExecutor()
}
