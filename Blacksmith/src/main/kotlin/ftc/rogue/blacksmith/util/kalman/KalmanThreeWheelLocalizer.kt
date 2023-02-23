package ftc.rogue.blacksmith.util.kalman

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer
import ftc.rogue.blacksmith.util.toRad

/**
 * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/util/kalmanfilters/kalmanfilter-localizers)
 */
class KalmanThreeWheelLocalizer(
    private val localizer: ThreeTrackingWheelLocalizer,
) : ThreeTrackingWheelLocalizer(localizer.getWheelPoses()) {

    private val wheelPos1Filter = KalmanFilter(9.0, 11.0)
    private val wheelPos2Filter = KalmanFilter(9.0, 11.0)
    private val wheelPos3Filter = KalmanFilter(9.0, 11.0)
    private val wheelPos1VelocityFilter = KalmanFilter(8.0, 7.0)
    private val wheelPos2VelocityFilter = KalmanFilter(8.0, 7.0)
    private val wheelPos3VelocityFilter = KalmanFilter(8.0, 7.0)

    override fun getWheelPositions(): List<Double> {
        val poses = localizer.getWheelPositions()

        return listOf(
            wheelPos1Filter.filter(poses[0]),
            wheelPos2Filter.filter(poses[1]),
            wheelPos3Filter.filter(poses[2]),
        )
    }

    override fun getWheelVelocities(): List<Double> {
        val poses = localizer.getWheelVelocities()!!

        return listOf(
            wheelPos1VelocityFilter.filter(poses[0]),
            wheelPos2VelocityFilter.filter(poses[1]),
            wheelPos3VelocityFilter.filter(poses[2]),
        )
    }

    fun setWheelPos1FilterCoeffs(R: Double, Q: Double) = apply {
        wheelPos1Filter.setProcessNoise(R)
        wheelPos1Filter.setMeasurementNoise(Q)
    }

    fun setWheelPos2FilterCoeffs(R: Double, Q: Double) = apply {
        wheelPos2Filter.setProcessNoise(R)
        wheelPos2Filter.setMeasurementNoise(Q)
    }
    fun setWheelPos3FilterCoeffs(R: Double, Q: Double) = apply {
        wheelPos3Filter.setProcessNoise(R)
        wheelPos3Filter.setMeasurementNoise(Q)
    }
    fun setWheelPos1VelocityFilterCoeffs(R: Double, Q: Double) = apply {
        wheelPos1VelocityFilter.setProcessNoise(R)
        wheelPos1VelocityFilter.setMeasurementNoise(Q)
    }

    fun setWheelPos2VelocityFilterCoeffs(R: Double, Q: Double) = apply {
        wheelPos2VelocityFilter.setProcessNoise(R)
        wheelPos2VelocityFilter.setMeasurementNoise(Q)
    }

    fun setWheelPos3VelocityFilterCoeffs(R: Double, Q: Double) = apply {
        wheelPos3VelocityFilter.setProcessNoise(R)
        wheelPos3VelocityFilter.setMeasurementNoise(Q)
    }
    // -- INTERNAL --

    companion object {
        private fun ThreeTrackingWheelLocalizer.getWheelPoses(): List<Pose2d> {
            val lateralDistance = this::class.java.getField("LATERAL_DISTANCE").get(null) as Double
            val forwardOffset = this::class.java.getField("FORWARD_OFFSET").get(null) as Double

            return listOf(
                Pose2d(0.0, +lateralDistance / 2, 0.0),
                Pose2d(0.0, -lateralDistance / 2, 0.0),
                Pose2d(forwardOffset, 0.0, 90.toRad()),
            )
        }
    }
}
