package data;

public class RegulatorParameter {

    private double mcMinimumReverse = -20;
    private double mcMaximumReverse = -100;
    private double mcMinimumForward = 20;
    private double mcMaximumForward = 100;

    public RegulatorParameter(double mcMinimumReverse, double mcMaximumReverse, double mcMinimumForward, double mcMaximumForward) {
        this.mcMinimumReverse = mcMinimumReverse;
        this.mcMaximumReverse = mcMaximumReverse;
        this.mcMinimumForward = mcMinimumForward;
        this.mcMaximumForward = mcMaximumForward;
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
}
