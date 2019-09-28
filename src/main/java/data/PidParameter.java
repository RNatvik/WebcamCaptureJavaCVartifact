package data;

public class PidParameter extends Data {

    private int kp;
    private int ki;
    private int kd;

        public PidParameter(int kp, int ki, int kd) {
        super(DataType.PID_PARAMETER);
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public int getKp() {
        return kp;
    }

    public int getKi() {
        return ki;
    }

    public int getKd() {
        return kd;
    }
}
