package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;

public class Robot {
    private DcMotor motorFL;
    private DcMotor motorFR;
    private DcMotor motorBL;
    private DcMotor motorBR;
    private Gamepad gamepad;

    HardwareMap hardwareMap = null;
    public Robot(HardwareMap ahwMap, Gamepad gamepad1) {
        //controler
        this.gamepad = gamepad1;
        //Ni-chan! Don't forget hardware.
        this.motorFL = hardwareMap.get(DcMotor.class, "motorDriveFrontLeft");
        this.motorFR = hardwareMap.get(DcMotor.class, "motorDriveFrontRight");
        this.motorBL = hardwareMap.get(DcMotor.class, "motorDriveBackLeft");
        this.motorBR = hardwareMap.get(DcMotor.class, "motorDriveBackRight");
        //Set DIRECTION@MOVE.net
        this.motorFL.setDirection(DcMotor.Direction.FORWARD);
        this.motorFR.setDirection(DcMotor.Direction.REVERSE);
        this.motorBL.setDirection(DcMotor.Direction.REVERSE);
        this.motorBR.setDirection(DcMotor.Direction.FORWARD);
    }

    //Driver for the mecanum wheels set as a function.
    public void drive(float motorMove) {
        //Make sure can be slower cause might not work.
        motorMove = (motorMove > 1) ? 1 : motorMove;
        motorMove = (motorMove < 0) ? 0 : motorMove;

        //condition ? if true : if fails;
        float goPlaces = (float) Math.pow(Range.clip(-(gamepad.right_trigger - gamepad.left_trigger), -1, 1), 5);
        float rotate = (float) ((gamepad.right_bumper ? 1 : 0) - (gamepad.left_bumper ? 1 : 0));
        float sideToSide = (float) Math.pow(gamepad1.left_stick_x, 5);

        //Combos for mecanum wheels HH→NK
        float toMotorFL = goPlaces + sideToSide + rotate;
        float toMotorFR = goPlaces - sideToSide - rotate;
        float toMotorBL = goPlaces - sideToSide + rotate;
        float toMotorBR = goPlaces + sideToSide - rotate;

        // must keep things proportional when the sum for any wheel > 1
        float frontMax = Math.max(Math.abs(toMotorFL), Math.abs(toMotorFR));
        float rearMax = Math.max(Math.abs(toMotorBL), Math.abs(toMotorBR));
        float maxDrive = Math.max(frontMax, rearMax);

        //must be a thing
        maxDrive = (maxDrive > 1) ? maxDrive : 1;

        //do thong so can drive
        toMotorFL = toMotorFL/maxDrive;
        toMotorFR = toMotorFR/maxDrive;
        toMotorBL = toMotorBL/maxDrive;
        toMotorBR = toMotorBR/maxDrive;

        //drive
        motorFL.setPower(toMotorFL * motorMove);
        motorFR.setPower(toMotorFR * motorMove);
        motorBL.setPower(toMotorBL * motorMove);
        motorBR.setPower(toMotorBR * motorMove);
    }
}
