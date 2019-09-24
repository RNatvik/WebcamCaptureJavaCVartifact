package data;

public class pidParameter extends Data {

    private int kp;
    private int ki;
    private int kd;

    public pidParameter(int kp, int ki, int kd) {
        super(DataType.PID_PARAMETER);
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }
}
