package data;

public class ControlInput extends Data{

    private boolean manualControl;
    private double forwardSpeed;
    private double turnSpeed;

    public ControlInput(boolean manualControl, double forwardSpeed, double turnSpeed) {
        this.manualControl = manualControl;
        this.forwardSpeed = forwardSpeed;
        this.turnSpeed = turnSpeed;
    }

    public boolean isManualControl() {
        return manualControl;
    }

    public void setManualControl(boolean manualControl) {
        this.manualControl = manualControl;
    }

    public double getForwardSpeed() {
        return forwardSpeed;
    }

    public void setForwardSpeed(double forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public double getTurnSpeed() {
        return turnSpeed;
    }

    public void setTurnSpeed(double turnSpeed) {
        this.turnSpeed = turnSpeed;
    }
}
