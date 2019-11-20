package data;

/**
 * Manual speed control class
 */
public class ControlInput extends Data {

    private boolean manualControl;
    private double forwardSpeed;
    private double turnSpeed;

    /**
     * Constructor
     *
     * @param manualControl flag for whether manual control is enabled
     * @param forwardSpeed  the target forward speed
     * @param turnSpeed     the target turn speed
     */
    public ControlInput(boolean manualControl, double forwardSpeed, double turnSpeed) {
        this.manualControl = manualControl;
        this.forwardSpeed = forwardSpeed;
        this.turnSpeed = turnSpeed;
    }

    /**
     * Gets the manual control flag
     *
     * @return the manual control flag
     */
    public boolean isManualControl() {
        return manualControl;
    }


    /**
     * Gets the target forward speed
     *
     * @return the target forward speed
     */
    public double getForwardSpeed() {
        return forwardSpeed;
    }

    /**
     * Gets the target turn speed
     *
     * @return the target turn speed
     */
    public double getTurnSpeed() {
        return turnSpeed;
    }
}
