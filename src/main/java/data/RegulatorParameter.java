package data;

public class RegulatorParameter extends Data{

    private double mcMinimumReverse;    //-20;
    private double mcMaximumReverse;    //-120;
    private double mcMinimumForward;    // 20;
    private double mcMaximumForward;    // 120;
    private double controllerMinOutput; // -200;
    private double controllerMaxOutput; //200
    private double ratio;               //1

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

    public double getRatio() {
        return ratio;
    }
}
