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

    private double setpoint;


    public PidParameter(double kp, double ki, double kd, double maxOutput, double minOutput, double setpoint) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.maxOutput = maxOutput;
        this.minOutput = minOutput;
        this.setpoint = setpoint;
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

    public double getMaxOutput() {
        return maxOutput;
    }

    public double getMinOutput() {
        return minOutput;
    }

    public double getSetpoint() {
        return setpoint;
    }

}
