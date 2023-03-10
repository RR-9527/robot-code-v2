package ftc.rogue.blacksmith

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import ftc.rogue.blacksmith.units.AngleUnit
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.units.TimeUnit
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import testutil.roadrunner.drive.SampleMecanumDrive
import testutil.roadrunner.trajectorysequence.TrajectorySequence
import testutil.roadrunner.trajectorysequence.TrajectorySequenceBuilder
import kotlin.math.PI

// TODO: More thorough testing
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AnvilTest {
    private val drive = SampleMecanumDrive(null)

    init {
        GlobalUnits.setUnits(DistanceUnit.INCHES, AngleUnit.RADIANS, TimeUnit.SECONDS)
    }

    @Test
    fun `roughly make sure Anvil's generated sequence is same as the actual builder`() {
        fun toExecute(anvil: Anvil) = anvil
            .forward(10)

        val expected = drive.trajectorySequenceBuilder(Pose2d())
            .forward(5.0)
            .turn(PI)
            .UNSTABLE_addTemporalMarkerOffset(1.0) {
                Thread.sleep(1000)
            }
            .waitSeconds(1.0)
            .setReversed(true)
            .splineTo(Vector2d(10.0, 10.0), PI)
            .setReversed(false)
            .forward(5.0)
            .turn(PI)
            .turn(PI)
            .forward(5.0)
            .turn(PI)
            .turn(PI)
            .forward(5.0)
            .turn(PI)
            .turn(PI)
            .addTemporalMarker({ 1.0 }) { }
            .addTrajectory(
                drive.trajectoryBuilder(Pose2d())
                    .forward(10.0)
                    .build()
            )
            .setReversed(true)
            .forward(10.0)
            .setReversed(false)
            .forward(10.0)
            .build()

        val actual = Anvil.forgeTrajectory(drive, Pose2d()) {
            forward(5.0)
            turn(PI)
            addTemporalMarker(1.0) {
                Thread.sleep(1000)
            }
            waitTime(1.0)
            inReverse {
                splineTo(10.0, 10.0, PI)
            }
            doTimes(3) {
                forward(5.0)
                doTimes(2) {
                    turn(PI)
                }
            }
            withRawBuilder<TrajectorySequenceBuilder> {
                addTemporalMarker({ 1.0 }) { }
            }
            addTrajectory {
                drive.trajectoryBuilder(Pose2d())
                    .forward(10.0)
                    .build()
            }
            forward(10).doInReverse()
            execute(::toExecute)
        }.build<TrajectorySequence>()

        assertAll(
            { assertEquals(expected.duration(), actual.duration(), 1e-6) },
            { assertEquals(expected.size(), actual.size()) },
            { assertTrue(expected.end() epsilonEquals actual.end()) },
        )
    }
}
