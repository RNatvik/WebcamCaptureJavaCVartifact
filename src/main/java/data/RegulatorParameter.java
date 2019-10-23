package data;

public class RegulatorParameter extends Data{

    private double mcMinimumReverse = -20;
    private double mcMaximumReverse = -100;
    private double mcMinimumForward = 20;
    private double mcMaximumForward = 100;
    private double controllerMinOutput = -200;
    private double controllerMaxOutput = 200;

    public RegulatorParameter(double mcMinimumReverse, double mcMaximumReverse, double mcMinimumForward, double mcMaximumForward, double controllerMinOutput, double controllerMaxOutput) {
        super(Data.REG_PARAM);
        this.mcMinimumReverse = mcMinimumReverse;
        this.mcMaximumReverse = mcMaximumReverse;
        this.mcMinimumForward = mcMinimumForward;
        this.mcMaximumForward = mcMaximumForward;
        this.controllerMinOutput = controllerMinOutput;
        this.controllerMaxOutput = controllerMaxOutput;
    }

    public double getMcMinimumReverse() {
        return mcMinimumReverse;
    }

    public double getMcMaximumReverse() {
        return mcMaximumReverse;
    }

    public double getMcMinimumForward() {
        return mcMinimumForward;
    }

    public double getMcMaximumForward() {
        return mcMaximumForward;
    }

    public double getControllerMinOutput() {
        return controllerMinOutput;
    }

    public double getControllerMaxOutput() {
        return controllerMaxOutput;
    }
}
