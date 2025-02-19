package org.firstinspires.ftc.teamcode;

/**
 * Author: Daan Stevenson
 * Created: 11/17/19
 * Goal: Control for Mecanum wheel drivetrain
 * Joystick inputs can be altered for x, y, and rotation
 */

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "MecanumDrive", group = "")
@Disabled

public class MecanumDrive extends LinearOpMode {

    // initialize I/O
    private DcMotor motorDriveBackLeft;
    private DcMotor motorDriveBackRight;
    private DcMotor motorDriveFrontLeft;
    private DcMotor motorDriveFrontRight;

    // slow mode string
    private String slow_mode = "DISENGAGED";

    @Override
    public void runOpMode() {

        // reference Configuration variables
        motorDriveBackLeft = hardwareMap.dcMotor.get("motorDriveBackLeft");
        motorDriveBackRight = hardwareMap.dcMotor.get("motorDriveBackRight");
        motorDriveFrontLeft = hardwareMap.dcMotor.get("motorDriveFrontLeft");
        motorDriveFrontRight = hardwareMap.dcMotor.get("motorDriveFrontRight");

        // set motor directions
        motorDriveBackLeft.setDirection(DcMotor.Direction.REVERSE);
        motorDriveBackRight.setDirection(DcMotor.Direction.FORWARD);
        motorDriveFrontLeft.setDirection(DcMotor.Direction.FORWARD);
        motorDriveFrontRight.setDirection(DcMotor.Direction.REVERSE);

        // reset encoders
        motorDriveBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDriveBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDriveFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDriveFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // use encoders
        motorDriveBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDriveBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDriveFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDriveFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // coefficients
        boolean slow_pressed = false;
        float slowDownCoeff = 1;

        // initializing telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            // logic to engage slow mode (no debounce)
            if (gamepad1.left_bumper) {
                if (!slow_pressed) {
                    if (slowDownCoeff == 1){
                        slowDownCoeff = (float)0.3;
                        slow_mode = "ENGAGED";
                    } else {
                        slowDownCoeff = (float)1.0;
                        slow_mode = "DISENGAGED";
                    }
                    slow_pressed = true;
                }
            } else {
                slow_pressed = false;
            }

            // load inputs from gamepad
            float for_bak = -(gamepad1.right_stick_y + gamepad1.left_stick_y);
            for_bak = Range.clip (for_bak, -1, 1);
            float crab = gamepad1.right_stick_x;
            float rotate = gamepad1.left_stick_x;

            // raise power to improve low level stick response
            for_bak = (float) Math.pow ( for_bak, 5);
            crab = (float) Math.pow ( crab, 5);
            rotate = (float) Math.pow ( rotate , 7);

            // combos for mecanum wheels
            float leftFront = for_bak + crab + rotate;
            float rightFront = for_bak - crab - rotate;
            float leftRear = for_bak - crab + rotate;
            float rightRear = for_bak + crab - rotate;

            // must keep things proportional when the sum for any wheel > 1
            float frontMax = Math.max(Math.abs(leftFront), Math.abs(rightFront));
            float rearMax = Math.max(Math.abs(leftRear), Math.abs(rightRear));
            float maxDrive = Math.max(frontMax, rearMax);
            // maxDrive = Range.clip(maxDrive, 1, 3);
            maxDrive = (maxDrive > 1) ? maxDrive : 1;

            // scale and clip all motors
            leftFront = leftFront/maxDrive;
            rightFront = rightFront/maxDrive;
            leftRear = leftRear/maxDrive;
            rightRear = rightRear/maxDrive;
            /*leftFront = Range.clip(leftFront, -1, 1);
            rightFront = Range.clip(rightFront, -1, 1);
            leftRear = Range.clip(leftRear, -1, 1);
            rightRear = Range.clip(rightRear, -1, 1);*/

            // set motor powers
            motorDriveBackLeft.setPower(leftRear * slowDownCoeff);
            motorDriveBackRight.setPower(rightRear * slowDownCoeff);
            motorDriveFrontLeft.setPower(leftFront * slowDownCoeff);
            motorDriveFrontRight.setPower(rightFront * slowDownCoeff);

            // telemetry update
            telemetry.addData("Status", "Running");
            telemetry.addData("Slow Mode", slow_mode);
            telemetry.update();

        }

    }
}
