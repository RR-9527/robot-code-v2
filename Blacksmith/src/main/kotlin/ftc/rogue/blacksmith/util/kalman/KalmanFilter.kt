@file:Suppress("PrivatePropertyName")

package ftc.rogue.blacksmith.util.kalman

import com.qualcomm.robotcore.util.ElapsedTime
import ftc.rogue.blacksmith.internal.util.MotionModel

/**
 * [Docs link](https://blacksmithftc.vercel.app/util/kalmanfilters/kalmanfilter-object)
 *
 * A Kalman Filter for 1D data.
 * It can be used for multiple dimensioned data, however a separate
 * object must be created for each dimension.
 *
 * @author TLindauer
 */
class KalmanFilter {
    /**
     * Vector A
     */
    private var A = 1.0

    /**
     * Vector B
     */
    private var B = 0.0

    /**
     * Vector C
     */
    private var C = 1.0

    /**
     * Process noise
     */
    private var R: Double

    /**
     * Measurement noise
     */
    private var Q: Double

    /**
     * Covariance variable
     */
    private var cov = Double.NaN

    /**
     * State variable
     */
    private var x = Double.NaN // estimated signal without noise
        set(value) {
            field = model(value, timer)
        }

    private lateinit var timer: ElapsedTime

    private var model: MotionModel =
        { x, _ -> x }

    /**
     * Instead of specifying a deviceCode, make a custom Kalman Filter.
     * @param R is process noise
     * @param Q is measurement noise
     * @param A is state vector
     * @param B is control vector
     * @param C is measurement vector
     */
    @JvmOverloads
    constructor(R: Double, Q: Double, A: Double, B: Double, C: Double, model: MotionModel = defaultModel) {
        this.R = R
        this.Q = Q
        this.A = A
        this.B = B
        this.C = C
        this.model = model
    }

    /**
     * Only specify noise
     * @param R is process noise
     * @param Q is measurement noise
     */
    // R is process noise, Q is measurement noise. No specified state/control/measurement vectors, set to default 1,0,1
    @JvmOverloads
    constructor(R: Double, Q: Double, model: MotionModel = defaultModel) {
        this.R = R
        this.Q = Q
        this.model = model
    }

    /**
     * Feed a new value into the Kalman filter and return what the predicted state is.
     * @param measurement the measured value
     * @param u is the controlled input value
     * @return the predicted result.
     * Postcondition: the appropriate filtered value has been returned
     */
    // Filter a measurement: measured value is measurement, controlled input value is u.
    @JvmOverloads
    fun filter(measurement: Double, u: Double = 0.0): Double {
        if (x.isNaN()) {
            if (!::timer.isInitialized) {
                timer = ElapsedTime()
            }

            x = measurement / C
            cov = Q / (C * C)
        } else {
            val predX = A * x + B * u
            val predCov = A * cov * A + R

            // Kalman gain
            val K = predCov * C / (C * predCov * C + Q)

            // Correction
            x = predX + K * (measurement - C * predX)
            cov = predCov - K * C * predCov
        }

        return x
    }

    val lastMeasurement: Double
        get() = x

    fun setMeasurementNoise(noise: Double) {
        Q = noise
    }

    fun setProcessNoise(noise: Double) {
        R = noise
    }

    fun setMotionModel(model: MotionModel) {
        this.model = model
    }

      companion object
    { val defaultModel: MotionModel =
        { x, _ -> x
        } }
}
