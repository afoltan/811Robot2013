/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.team811.devices;

import edu.wpi.first.team811.vars.Config;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author saswat
 */
public final class EncoderDrivetrain implements Config{

    private Encoder left, right;
    private SpeedController left_motor, right_motor;
    private PIDController left_pid, right_pid;
    private int max_speed = DRIVE_MAX_SPEED;
    
    private double[] l_i = new double[3];
    private double[] r_i = new double[3];

    /**
     * Creates an encoder drive train from ports defaults to Talon Motor
     * Controllers
     *
     * @param left_a left encoder a channel
     * @param left_encoder_b left encoder b channel
     * @param right_encoder_a right encoder a channel
     * @param right_encoder_b right encoder b channel
     * @param left_motor_port motor controller port on left side
     * @param right_motor_port motor controller port on right side
     */
    public EncoderDrivetrain(int left_encoder_a, int left_encoder_b, int right_encoder_a, int right_encoder_b, int left_motor_port, int right_motor_port) {
        this(new Encoder(left_encoder_a, left_encoder_b, true), new Encoder(right_encoder_a, right_encoder_b, true), new Talon(left_motor_port), new Talon(right_motor_port));
    }

    /**
     * Creates an encoder drive train from objects
     *
     * @param left encoder on left wheel
     * @param right encoder on right wheel
     * @param left_motor motor controller on left side
     * @param right_motor motor controller on right side
     */
    public EncoderDrivetrain(Encoder left, Encoder right, SpeedController left_motor, SpeedController right_motor) {
        this.left = left;
        this.right = right;
        this.left_motor = left_motor;
        this.right_motor = right_motor;

        left.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
        right.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);

        left_pid = new PIDController(DRIVE_PID_P, DRIVE_PID_I, DRIVE_PID_D,  new PIDSource() {

            public double pidGet() {
                l_i[2] = l_i[1];
                l_i[1] = l_i[0];
                l_i[0] = EncoderDrivetrain.this.left.getRate();
                
                return (l_i[2] + l_i[1] + l_i[0])/3;
                //return left_count.getRate();
            }
        }, left_motor);
        right_pid = new PIDController(DRIVE_PID_P, DRIVE_PID_I, DRIVE_PID_D,  new PIDSource() {

            public double pidGet() {
                r_i[2] = r_i[1];
                r_i[1] = r_i[0];
                r_i[0] = EncoderDrivetrain.this.right.getRate();
                
                return (r_i[2] + r_i[1] + r_i[0])/3;
                //return right_count.getRate();
            }
        }, right_motor);

        setMaxSpeed(max_speed);

        setSpeed(0, 0);
    }
    
    /**
     * Driving style comparable to a car
     * 
     * @param moveValue accelerator
     * @param rotateValue steering wheel
     */
    public void arcadeDrive(double moveValue, double rotateValue) {
        double leftMotorSpeed;
        double rightMotorSpeed;

        if (moveValue >= 0.0) {
            moveValue = (moveValue * moveValue);
        } else {
            moveValue = -(moveValue * moveValue);
        }
        if (rotateValue >= 0.0) {
            rotateValue = (rotateValue * rotateValue);
        } else {
            rotateValue = -(rotateValue * rotateValue);
        }

        if (moveValue > 0.0) {
            if (rotateValue > 0.0) {
                leftMotorSpeed = moveValue - rotateValue;
                rightMotorSpeed = Math.max(moveValue, rotateValue);
            } else {
                leftMotorSpeed = Math.max(moveValue, -rotateValue);
                rightMotorSpeed = moveValue + rotateValue;
            }
        } else {
            if (rotateValue > 0.0) {
                leftMotorSpeed = -Math.max(-moveValue, rotateValue);
                rightMotorSpeed = moveValue + rotateValue;
            } else {
                leftMotorSpeed = moveValue - rotateValue;
                rightMotorSpeed = -Math.max(-moveValue, -rotateValue);
            }
        }

        setSpeed(leftMotorSpeed * max_speed, rightMotorSpeed * max_speed);
    }
    
    /**
     * Starts the drive train
     */
    public void enable() {
        left_pid.enable();
        right_pid.enable();

        left.reset();
        right.reset();
        
        left.start();
        right.start();
    }
    
    /**
     * Stops the drive train
     */
    public void disable() {
        left_pid.disable();
        right_pid.disable();

        left.reset();
        right.reset();
        
        left.stop();
        right.stop();
        
        setSpeed(0, 0);
    }

    
    /**
     * Method to access the left encoder
     * @return the left side encoder
     */
    public Encoder getLeftEncoder() {
        return left;
    }

    /**
     * Method to access the right encoder
     * @return the right side encoder
     */
    public Encoder getRightEncoder() {
        return right;
    }
    
    /**
     * Method to access the left motor controller
     * @return the left side motor controller
     */
    public SpeedController getLeftMotor() {
        return left_motor;
    }

    /**
     * Method to access the right motor controller
     * @return the right side motor controller
     */
    public SpeedController getRightMotor() {
        return right_motor;
    }
    
    /**
     * Convenience method, sets distance per pulse for both encoders
     *
     * @param distance distance per pulse for both encoders
     */
    public void setDistancePerPulse(double distance) {
        setDistancePerPulse(distance, distance);
    }

    /**
     * Sets distance per pulse for the left and right encoders
     *
     * @param left_distance distance per pulse for the left encoder
     * @param right_distance distance per pulse for the right encoder
     */
    public void setDistancePerPulse(double left_distance, double right_distance) {
        left.setDistancePerPulse(left_distance);
        right.setDistancePerPulse(right_distance);
    }
    
    /**
     * Set the maximum speed of the robot, used for the PID loop
     * @param speed the maximum speed
     */
    public void setMaxSpeed(int speed) {
        max_speed = speed;
        left_pid.setInputRange(-max_speed, max_speed);
        left_pid.setOutputRange(-1, 1);

        right_pid.setInputRange(-max_speed, max_speed);
        right_pid.setOutputRange(-1, 1);
    }

    /**
     * Tank drive
     * 
     * @param left_speed left side speed
     * @param right_speed right side speed
     */
    public void setSpeed(double left_speed, double right_speed) {
        //left_pid.setSetpoint(-left_speed);
        setLeftSpeed(left_speed);
        //right_pid.setSetpoint(right_speed);
        setRightSpeed(right_speed);
    }
    
    /**
     * Sets speed on the left side
     * 
     * @param left_speed left side speed
     */
    public void setLeftSpeed(double left_speed) {
        left_pid.setSetpoint(-left_speed);
    }
    
    /**
     * Sets speed on the right side
     * 
     * @param right_speed right side speed
     */
    public void setRightSpeed(double right_speed) {
        right_pid.setSetpoint(right_speed);
    }
    
    public boolean autoDrive(double left_distance, double right_distance) {
        boolean left_done = false;
        boolean right_done = false;
        if (left.getDistance() < left_distance) {
            //left_jag.set(.4);
            //left_pid.setSetpoint(40);
            setLeftSpeed(40);
        } else {
            //left_jag.set(0);
            //left_pid.setSetpoint(0);
            setLeftSpeed(0);
            left_done = true;
        }
        if (right.getDistance() > right_distance * -1) {
            //right_jag.set(-.4);
            //right_pid.setSetpoint(-40);
            setRightSpeed(40);
        } else {
            //right_jag.set(0);
            //right_pid.setSetpoint(0);
            setRightSpeed(0);
            right_done = true;
        }


        //if (left_count.getDistance() > left_distance && right_count.getDistance() > right_distance*-1) {
        if (left_done && right_done) {
            return true;
        }
        return false;
    }
    
}
