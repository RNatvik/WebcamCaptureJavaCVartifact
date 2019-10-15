package Regulering;

import java.util.TimerTask;

import static java.lang.Math.sin;

public class DataInProduser extends TimerTask {

    private StorageBox sb;
    private int value;
    private int maxValue = 2000;

    public DataInProduser(StorageBox sb){
        this.sb = sb;

    }



    @Override
    public void run() {
        if (value < maxValue) {
            // produce new values up to maxValue
            System.out.println("Producer # requests to put value");
            synchronized (sb) {
                // storageBox is the shared resource (critical) and must be synchronized
                if (!sb.getAvailable()) {
                    // conditionally put new value
                    double input = 100 + 20*sin(value);
                    sb.put(input);
                    System.out.println("Producer put: " + input);
                    value++;
                }
            }
        }
    }
}
