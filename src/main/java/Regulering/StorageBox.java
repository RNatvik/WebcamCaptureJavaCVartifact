package Regulering;

/**
 * Simple test to test my understanding in timer, timertask.
 *
 * @author LB
 */



public class StorageBox {

    private double setpoint;       // value to be stored
    private int controllerOutput;
    private boolean available;  // flag

    public StorageBox() {
        this.available = false;
    }

    public double getValue() {
        setAvailable(false);
        return this.setpoint;
    }

    public boolean getAvailable() {
        return this.available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void put(double value) {
        this.setpoint = value; // store value
        setAvailable(true);
    }
}
