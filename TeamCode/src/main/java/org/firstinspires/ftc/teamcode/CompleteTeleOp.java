package org.firstinspires.ftc.teamcode;

/**
 * Author: Pinhead FTC
 * Created: 12/2/19
 * Goal: Messy compilation of all code necessary for TeleOp Control
 */

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "CompleteTeleOp", group = "")

public class CompleteTeleOp extends LinearOpMode {

    // initialize I/O
    private DcMotor motorDriveBackLeft;
    private DcMotor motorDriveBackRight;
    private DcMotor motorDriveFrontLeft;
    private DcMotor motorDriveFrontRight;
    private DcMotorSimple motorElevator;
    private DigitalChannel digElevatorLimit;
    private DistanceSensor sensorRange;
    Servo servoGripper;
    Servo   servoTray;

    // slow mode string
    private String slow_mode = "DISENGAGED";

    // gripper states
    double gripper_open = 0.4;
    double gripper_closed = 0.8;
    boolean gripper_press = false;
    private String gripper_state = "OPEN";

    // initialize I/O
    double tray_up = 0.8;
    double tray_down = 0.3;
    boolean tray_press = false;
    private String tray_state = "UP";

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

        // reference Configuration variables
        motorElevator = hardwareMap.get(DcMotorSimple.class, "motorElevator");
        digElevatorLimit = hardwareMap.get(DigitalChannel.class, "digElevatorLimit");
        servoGripper = hardwareMap.get(Servo.class, "servoGripper");
        servoTray = hardwareMap.get(Servo.class, "servoTray");
        sensorRange = hardwareMap.get(DistanceSensor.class, "sensor_range");

        // set the digital channel to input.
        digElevatorLimit.setMode(DigitalChannel.Mode.INPUT);
        motorElevator.setDirection(DcMotorSimple.Direction.REVERSE);

        // lower elevator until limit switch is depressed
        while (digElevatorLimit.getState() == false) {
            motorElevator.setPower(-0.6);
        }
        motorElevator.setPower(0);
        double elevatorInput = 0;

        // initialize tray servo position
        servoGripper.setPosition(gripper_open);
        servoTray.setPosition(tray_up);

        // slow mode coefficients
        boolean slow_pressed = false;
        float slowDownCoeff = 1;

        // initializing telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
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

            // manual joystick control
            elevatorInput = -gamepad2.left_trigger + gamepad2.right_trigger;
            double measured_distance = sensorRange.getDistance(DistanceUnit.MM);

            // if limit switch is depressed, elevator can only raise and level is set to 0
            if (digElevatorLimit.getState() == true) {
                elevatorInput = Range.clip(elevatorInput, 0, 1);
            } else if (measured_distance < 20) {
                elevatorInput = Range.clip(elevatorInput, -0.6, 1);
            } else if (measured_distance > 310) {
                elevatorInput = Range.clip(elevatorInput, -1, 0);
            } else if (measured_distance > 300) {
                elevatorInput = Range.clip(elevatorInput, -1, 0.5);
            }

            // set elevator motor speed
            motorElevator.setPower(elevatorInput);

            // gripper servo logic & control
            if (gripper_press == false) {
                if (gamepad2.dpad_left == true) {
                    servoGripper.setPosition(gripper_open);
                    gripper_state = "OPEN";
                    gripper_press = true;
                } else if (gamepad2.dpad_right == true) {
                    servoGripper.setPosition(gripper_closed);
                    gripper_state = "CLOSED";
                    gripper_press = true;
                } else {
                    gripper_press = false;
                }
            } else{
                if (gamepad2.dpad_left == false && gamepad2.dpad_right == false){
                    gripper_press = false;
                }
            }

            // tray servo logic & control
            if (tray_press == false) {
                if (gamepad2.y == true) {
                    servoTray.setPosition(tray_up);
                    tray_state = "UP";
                    tray_press = true;
                } else if (gamepad2.a == true) {
                    servoTray.setPosition(tray_down);
                    tray_state = "DOWN";
                    tray_press = true;
                } else {
                    tray_press = false;
                }
            } else{
                if (gamepad2.y == false && gamepad2.a == false){
                    tray_press = false;
                }
            }

            // telemetry update
            telemetry.addData("Status", "Running");
            telemetry.addData("Block gripper", String.valueOf(gripper_state));
            telemetry.addData("Tray servo", String.valueOf(tray_state));
            telemetry.addData("Elevator height", String.valueOf(measured_distance));
            telemetry.addData("Slow mode", slow_mode);
            telemetry.update();
        }
    }
}
