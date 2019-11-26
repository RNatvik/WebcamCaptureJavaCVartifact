package data;

/**
 * This class contains all configuration parameters for a PID controller.
 */
public class PidParameter extends Data {


    private double kp;
    private double ki;
    private double kd;

    private double maxOutput;
    private double minOutput;
    private double deadBand;
    private double maxIOutput;
    private boolean reversed;
    private double setpoint;

    /**
     * Constructor
     *
     * @param kp         proportional gain
     * @param ki         integral gain
     * @param kd         derivative gain
     * @param maxOutput  max output
     * @param minOutput  min output
     * @param setpoint   pid setpoint
     * @param deadBand   deadband
     * @param maxIOutput max value as result from I component of PID regulator
     * @param reversed   reverse the values (x * -1)
     */
    public PidParameter(double kp,
                        double ki,
                        double kd,
                        double maxOutput,
                        double minOutput,
                        double setpoint,
                        double deadBand,
                        double maxIOutput,
                        boolean reversed) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.maxOutput = maxOutput;
        this.minOutput = minOutput;
        this.setpoint = setpoint;
        this.deadBand = deadBand;
        this.maxIOutput = maxIOutput;
        this.reversed = reversed;
    }

    /**
     * Set proportional gain
     *
     * @param kp new proportional gain
     */
    public void setKp(double kp) {
        this.kp = kp;
    }

    /**
     * Set integral gain
     *
     * @param ki new integral gain
     */
    public void setKi(double ki) {
        this.ki = ki;
    }

    /**
     * Set derivative gain
     *
     * @param kd new derivative gain
     */
    public void setKd(double kd) {
        this.kd = kd;
    }

    /**
     * Returns the instance's proportional gain value
     *
     * @return the instance's proportional gain value
     */
    public double getKp() {
        return kp;
    }

    /**
     * Returns the instance's integral gain value
     *
     * @return the instance's integral gain value
     */
    public double getKi() {
        return ki;
    }

    /**
     * Returns the instance's derivative gain value
     *
     * @return the instance's derivative gain value
     */
    public double getKd() {
        return kd;
    }

    /**
     * Returns the instance's max output
     *
     * @return the instance's max output
     */
    public double getMaxOutput() {
        return maxOutput;
    }

    /**
     * Returns the instance's min output
     *
     * @return the instance's min output
     */
    public double getMinOutput() {
        return minOutput;
    }

    /**
     * Returns the instance's setpoint value
     *
     * @return the instance's setpoint value
     */
    public double getSetpoint() {
        return setpoint;
    }

    /**
     * Returns the instance's deadband value
     *
     * @return the instance's deadband value
     */
    public double getDeadBand() {
        return deadBand;
    }

    /**
     * Returns the instance's max integral output
     *
     * @return the instance's max integral output
     */
    public double getMaxIOutput() {
        return maxIOutput;
    }

    /**
     * Returns the instance's reversed flag
     *
     * @return the instance's reversed flag
     */
    public boolean isReversed() {
        return reversed;
    }
}
