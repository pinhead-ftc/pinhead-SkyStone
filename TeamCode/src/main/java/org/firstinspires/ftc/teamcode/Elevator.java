package org.firstinspires.ftc.teamcode;

/**
 * Author: Daan Stevenson
 * Created: 11/16/19
 * Goal: Move the elevator in discrete steps
 * Motor is controlled by REV Spark Mini
 * Limit switch is used to locate lowest position
 * Currently Non-Functional - error = "Stuck in stop() -- may be servo?"
 */

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "Elevator", group = "")
@Disabled

public class Elevator extends LinearOpMode {

    // initialize I/O
    private DcMotorSimple motorElevator;
    private DigitalChannel digElevatorLimit;
    // runtime variable
    private ElapsedTime elevatorRuntime = new ElapsedTime();

    @Override
    public void runOpMode() {

        // reference Configuration variables
        motorElevator = hardwareMap.get(DcMotorSimple.class, "motorElevator");
        digElevatorLimit = hardwareMap.get(DigitalChannel.class, "digElevatorLimit");

        // set the digital channel to input.
        digElevatorLimit.setMode(DigitalChannel.Mode.INPUT);
        motorElevator.setDirection(DcMotorSimple.Direction.REVERSE);

        // lower elevator until limit switch is depressed
        while (digElevatorLimit.getState() == false) {
            motorElevator.setPower(-0.4);
        }
        motorElevator.setPower(0);
        float elevatorInput = 0;

        // Setup an integer to define block level
        // 0 = lowest (on ground)
        // 1 = up 1 block level
        int elevatorLevel = 0;
        int elevatorDesiredLevel = 0;
        boolean gamepad_press = false;
        double level_duration = 600;

        // initializing telemetry
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // elevator set level logic
            if (gamepad_press == false) {
                if (gamepad1.dpad_up == true) {
                    elevatorDesiredLevel++;
                    gamepad_press = true;
                } else if (gamepad1.dpad_down == true) {
                    elevatorDesiredLevel--;
                    gamepad_press = true;
                } else {
                    gamepad_press = false;
                }
            } else{
                if (gamepad1.dpad_up == false && gamepad1.dpad_down == false){
                    gamepad_press = false;
                }
            }
            elevatorDesiredLevel = (int) Range.clip(elevatorDesiredLevel, 0, 3) ;

            // elevator run logic
            if (elevatorDesiredLevel > elevatorLevel) { // raise
                if (elevatorInput == 0) { // initialize run
                    elevatorRuntime.reset();
                    elevatorInput = 1;
                } else { // was already running
                    if (elevatorRuntime.milliseconds() > level_duration) {
                        if (elevatorInput > 0) { // rising
                            elevatorLevel++;
                        } else { // lowering
                            elevatorLevel--;
                        }
                        elevatorInput = 0;
                    }
                }
            } else if (elevatorDesiredLevel < elevatorLevel) { // lower
                if (elevatorInput == 0) { // initialize run
                    elevatorRuntime.reset();
                    elevatorInput = -1;
                } else { // was already running
                    if (elevatorRuntime.milliseconds() > level_duration) {
                        if (elevatorInput > 0) { // rising
                            elevatorLevel++;
                        } else { // lowering
                            elevatorLevel--;
                        }
                        elevatorInput = 0;
                    }
                }
            } else { // reset to desired level
                if (elevatorInput != 0) { // running
                    if (elevatorRuntime.milliseconds() > level_duration) {
                        if (elevatorInput > 0) { // rising
                            elevatorLevel++;
                        } else { // lowering
                            elevatorLevel--;
                        }
                        elevatorInput = 0;
                    }
                }
            }

            // manual joystick control (disabled)
            // double elevatorInput = -gamepad1.left_stick_y;

            // if limit switch is depressed, elevator can only raise and level is set to 0
            if (digElevatorLimit.getState() == true) {
                elevatorInput = Range.clip(elevatorInput, 0, 1);
                elevatorLevel = 0;
            }

            // set elevator motor speed
            motorElevator.setPower(elevatorInput);

            // telemetry update
            telemetry.addData("Status", "Running");
            telemetry.addData("Desired elevator Level", String.valueOf(elevatorDesiredLevel));
            telemetry.addData("Elevator Level", String.valueOf(elevatorLevel));
            telemetry.update();
        }
    }
}
