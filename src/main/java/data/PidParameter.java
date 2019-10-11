package data;

/**
 * This class contains all configuration parameters for a PID controller.
 */
public class PidParameter extends Data {

    private int kp;
    private int ki;
    private int kd;

    /**
     * Construct a new instance
     *
     * @param kp proportional gain
     * @param ki integral gain
     * @param kd derivative gain
     */
    public PidParameter(int kp, int ki, int kd) {
        super(Data.PID_PARAM);
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    /**
     * Set proportional gain
     *
     * @param kp new proportional gain
     */
    public void setKp(int kp) {
        this.kp = kp;
    }

    /**
     * Set integral gain
     *
     * @param ki new integral gain
     */
    public void setKi(int ki) {
        this.ki = ki;
    }

    /**
     * Set derivative gain
     *
     * @param kd new derivative gain
     */
    public void setKd(int kd) {
        this.kd = kd;
    }

    /**
     * Returns the instance's proportional gain value
     *
     * @return the instance's proportional gain value
     */
    public int getKp() {
        return kp;
    }

    /**
     * Returns the instance's integral gain value
     *
     * @return the instance's integral gain value
     */
    public int getKi() {
        return ki;
    }

    /**
     * Returns the instance's derivative gain value
     *
     * @return the instance's derivative gain value
     */
    public int getKd() {
        return kd;
    }
}
