package data;

public class RegulatorOutput extends Data {

    private double leftMotor;
    private double rightMotor;

    public RegulatorOutput(double leftMotor, double rightMotor) {
        super(Data.REGULATOR_OUTPUT);
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
    }

    public double getLeftMotor() {
        return leftMotor;
    }

    public double getRightMotor() {
        return rightMotor;
    }
}
