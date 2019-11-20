package data;

/**
 * Output from the regulator, which is sent to the motors
 */
public class RegulatorOutput extends Data {

    private double leftMotor;
    private double rightMotor;

    /**
     * Constructor
     *
     * @param leftMotor  the output value for the left motor
     * @param rightMotor the output value for the right motor
     */
    public RegulatorOutput(double leftMotor, double rightMotor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
    }

    /**
     * Returns the instance's left motor output value
     *
     * @return the instance's left motor output value
     */
    public double getLeftMotor() {
        return leftMotor;
    }

    /**
     * Returns the instance's right motor output value
     *
     * @return the instance's right motor output value
     */
    public double getRightMotor() {
        return rightMotor;
    }
}
