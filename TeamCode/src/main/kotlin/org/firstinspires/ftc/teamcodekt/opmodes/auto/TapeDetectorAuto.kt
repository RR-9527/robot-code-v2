
package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.kt.invoke
import ftc.rogue.blacksmith.util.toIn
import org.firstinspires.ftc.teamcode.R
import org.firstinspires.ftc.teamcode.pipelines.TapeDetector
import org.firstinspires.ftc.teamcodekt.components.height
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames
import org.firstinspires.ftc.teamcodekt.components.width
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation

@Autonomous
class TapeDetectorAuto : LinearOpMode() {
    val tapeDetectorPipeline = TapeDetector(BlackOp.mTelemetry)

    val camera: OpenCvCamera

    init {
        camera = OpenCvCameraFactory.getInstance().createWebcam(
            BlackOp.hwMap(DeviceNames.WEBCAM1),
            R.id.cameraMonitorViewId,
        )
        camera.setPipeline(tapeDetectorPipeline)

        camera.openCameraDeviceAsync(object : OpenCvCamera.AsyncCameraOpenListener {
            override fun onOpened() {
                camera.startStreaming(width, height, OpenCvCameraRotation.UPRIGHT)
            }

            override fun onError(errorCode: Int) {
                throw RuntimeException("Error opening camera! Error code $errorCode")
            }
        })
    }

    override fun runOpMode() {
        while(!opModeIsActive()){
            telemetry.addData("Angle", tapeDetectorPipeline.tapeAngle)
            telemetry.update()
        }
    }
}
