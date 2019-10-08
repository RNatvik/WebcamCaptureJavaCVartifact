package data;

public class PidParameter extends JSONData {

    private int kp;
    private int ki;
    private int kd;

    public PidParameter(int kp, int ki, int kd, boolean initialFlag) {
        super(initialFlag, Data.PID_PARAM);
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public void setKp(int kp) {
        this.kp = kp;
    }

    public void setKi(int ki) {
        this.ki = ki;
    }

    public void setKd(int kd) {
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
