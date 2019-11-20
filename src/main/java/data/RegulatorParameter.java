package data;

/**
 * Settings parameters for the regulator
 */
public class RegulatorParameter extends Data {

    private double mcMinimumReverse;    //-20;
    private double mcMaximumReverse;    //-120;
    private double mcMinimumForward;    // 20;
    private double mcMaximumForward;    // 120;
    private double controllerMinOutput; // -200;
    private double controllerMaxOutput; //200
    private double ratio;               //1

    /**
     * Constructor
     *
     * @param mcMinimumReverse    minimum reverse output value for motor controller
     * @param mcMaximumReverse    maximum reverse output value for motor controller
     * @param mcMinimumForward    minimum forward output value for motor controller
     * @param mcMaximumForward    maximum forward output value for motor controller
     * @param controllerMinOutput minimum controller output
     * @param controllerMaxOutput maximum controller output
     * @param ratio               ratio between forward and turn PID
     */
    public RegulatorParameter(double mcMinimumReverse,
                              double mcMaximumReverse,
                              double mcMinimumForward,
                              double mcMaximumForward,
                              double controllerMinOutput,
                              double controllerMaxOutput,
                              double ratio) {

        this.mcMinimumReverse = mcMinimumReverse;
        this.mcMaximumReverse = mcMaximumReverse;
        this.mcMinimumForward = mcMinimumForward;
        this.mcMaximumForward = mcMaximumForward;
        this.controllerMinOutput = controllerMinOutput;
        this.controllerMaxOutput = controllerMaxOutput;
        this.ratio = Math.max(0, Math.min(1, ratio));


    }

    /**
     * Returns the instance's minimum reverse motor controller output value
     *
     * @return the instance's minimum reverse motor controller output value
     */
    public double getMcMinimumReverse() {
        return mcMinimumReverse;
    }

    /**
     * Returns the instance's maximum reverse motor controller output value
     *
     * @return the instance's maximum reverse motor controller output value
     */
    public double getMcMaximumReverse() {
        return mcMaximumReverse;
    }

    /**
     * Returns the instance's minimum forward motor controller output value
     *
     * @return the instance's minimum forward motor controller output value
     */
    public double getMcMinimumForward() {
        return mcMinimumForward;
    }

    /**
     * Returns the instance's maximum forward motor controller output value
     *
     * @return the instance's maximum forward motor controller output value
     */
    public double getMcMaximumForward() {
        return mcMaximumForward;
    }

    /**
     * Returns the instance's minimum controller output
     *
     * @return the instance's minimum controller output
     */
    public double getControllerMinOutput() {
        return controllerMinOutput;
    }

    /**
     * Returns the instance's maximum controller output
     *
     * @return the instance's maximum controller output
     */
    public double getControllerMaxOutput() {
        return controllerMaxOutput;
    }

    /**
     * Returns the instance's forward to turn ratio
     *
     * @return the instance's forward to turn ratio
     */
    public double getRatio() {
        return ratio;
    }
}
