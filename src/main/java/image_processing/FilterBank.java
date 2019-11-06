package image_processing;

import java.util.HashMap;
import java.util.Map;

public class FilterBank {

    private Map<String, double[]> kernelMap;
    private Map<String, double[]> signalMap;

    public FilterBank() {
        this.kernelMap = new HashMap<>();
        this.signalMap = new HashMap<>();
    }

    public void registerSignal(String id, double[] kernel) {
        this.kernelMap.put(id, kernel);
        double[] signal = new double[kernel.length];
        this.signalMap.put(id, signal);

        //System.out.print("Registered \"" + id + "\" Kernel: [");
        for (double value : kernel) {
            //System.out.print(" " + value + " ");
        }
        //System.out.print(" ] Signal: [");
        for (double v : signal) {
            //System.out.print(" " + v + " ");
        }
        //System.out.println(" ]");
    }

    public double passValue(String id, double value) {

        double[] kernel = this.kernelMap.get(id);
        double[] signal = this.signalMap.get(id);
        for (int i = signal.length - 2; i >= 0; i--) {
            signal[i+1] = signal[i];
        }
        signal[0] = value;
        double sum = 0;
        for (int i = 0; i < signal.length; i++) {
            sum += signal[i]*kernel[i];
        }
        return sum;
    }

    public boolean idAvailable(String id) {
        boolean success = false;
        if (!this.kernelMap.containsKey(id)) {
            success = true;
        }
        return success;
    }
}
