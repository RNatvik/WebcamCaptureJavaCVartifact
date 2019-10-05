package image_processing;

/**
 * This class represents a low pass filter for signals
 */
public class LowPassFilter {

    private int[] values;
    private int index;

    public LowPassFilter(int filterLength) {
        this.values = new int[filterLength];
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = 0;
        }
        this.index = 0;
    }

    public int passValue(int input) {
        this.values[this.index] = input;
        this.index++;

        if (this.index >= this.values.length) {
            this.index = 0;
        }

        int avg;
        try {
             avg = getAverage();
        } catch (ArithmeticException e) {
            avg = 0;
        }

        return avg;
    }

    public int passValue() {
        int avg;
        try {
            avg = getAverage();
        } catch (ArithmeticException e) {
            avg = 0;
        }
        return avg;
    }

    private int getAverage() throws ArithmeticException {
        long sum = 0;
        int max = 0;
        int min = 2147483647;
        for (int value : this.values) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        long avg = (sum - max - min) / (this.values.length - 2);
        return Math.toIntExact(avg);
    }
}
