package Regulering;

import data.PidParameter;

/**
 *

 * Taken from https://github.com/tekdemo/MiniPID-Java/blob/master/src/com/stormbots/MiniPID.java
 * and http://brettbeauregard.com/blog/2011/04/improving-the-beginners-pid-direction/improving-the-beginners-pid-introduction
 * <p>
 * changed by LB.
 */

public class PID {


    private double maxIOutput = 0;
    private double maxError = 0;
    private double errorSum = 0;

    private double lastError = 0;
    private long lastRun = 0;
    private boolean firstRun = true;
    private double lastOutput = 0;
    private PidParameter parameters;


    public PID(PidParameter parameters) {
        this.parameters = parameters;
    }




    /**
     * Calculate the output value for the current PID cycle.<br>
     *
     * @param actual The monitored value, typically as a sensor input.
     * @return calculated output value for driving the system
     */
    public double getOutput(double actual) {
        // Calculate how long time since we last calculated:
        long now = System.currentTimeMillis();
        long dt = now - lastRun;
        lastRun = now;

        // Define and extract variables used in calculation
        double output;
        double Poutput;
        double Ioutput;
        double Doutput;
        double setpoint = this.parameters.getSetpoint();
        double P = this.parameters.getKp();
        double I = this.parameters.getKi();
        double D = this.parameters.getKd();
        double minOutput = this.parameters.getMinOutput();
        double maxOutput = this.parameters.getMaxOutput();
        double deadBand = this.parameters.getDeadBand();

        // Calculate the error
        double error = setpoint - actual;

        if (deadBand != 0) {
            if (Math.abs(error) < deadBand) {
                error = 0;
            }
        }

        // Calculate P term
        Poutput = P * error;

        // If this is our first time running this, we don't actually _have_ a previous input or output.
        if (firstRun) {
            lastError = error;
            lastOutput = Poutput;
            firstRun = false;
            dt = 0;
        }

        // Calculate D Term
        double errorChange = (error - lastError);
        if ((errorChange != 0) && (dt != 0)) {
            Doutput = D * errorChange / dt;
        } else {
            Doutput = 0;
        }
        lastError = error;

        // 1. maxIoutput restricts the amount of output contributed by the Iterm.
        // 2. prevent windup by not increasing errorSum if we're already running against our max Ioutput
        // 3. prevent windup by not increasing errorSum if output is output=maxOutput
        Ioutput = I * errorSum * dt;
        if (maxIOutput != 0) {
            Ioutput = constrain(Ioutput, -maxIOutput, maxIOutput);
        }
        //Add up the terms up
        output = Poutput + Ioutput + Doutput;


        if (maxIOutput != 0) {
            errorSum = constrain(errorSum + error, -maxError, maxError);
            // In addition to output limiting directly, we also want to prevent I term
            // buildup, so restrict the error directly
        } else {
            errorSum += error;
        }

        if (minOutput != maxOutput) {
            output = constrain(output, minOutput, maxOutput);
        }

        // Get a test printline with lots of details about the internal
        // calculations. This can be useful for debugging.
        // System.out.printf("Final output %5.2f [ %5.2f, %5.2f , %5.2f  ], eSum %.2f\n", output, Poutput, Ioutput, Doutput, errorSum);
        // System.out.printf("%5.2f\t%5.2f\t%5.2f\t%5.2f\n", output, Poutput, Ioutput, Doutput);

        lastOutput = output;
        return output;
    }


    /**
     * Resets the controller. This erases the I term buildup, and removes
     * D gain on the next loop.<br>
     * This should be used any time the PID is disabled or inactive for extended
     * duration, and the controlled portion of the system may have changed due to
     * external forces.
     */
    public void reset() {
        firstRun = true;
        errorSum = 0;
        lastRun = 0;
    }


    //**************************************
    // Helper functions
    //**************************************

    /**
     * Forces a value into a specific range
     *
     * @param value input value
     * @param min   maximum returned value
     * @param max   minimum value in range
     * @return Value if it's within provided range, min or max otherwise
     */
    private double constrain(double value, double min, double max) {
        if (value > max) {
            return max;
        }
        if (value < min) {
            return min;
        }
        return value;
    }

    /**
     * To operate correctly, all PID parameters require the same sign
     * This should align with the {@literal}reversed value
     */
    private PidParameter checkSigns(PidParameter pp) {
        if (pp.isReversed()) {
            if (pp.getKp() > 0) {
                pp.setKp(pp.getKp() * (-1));
            }
            if (pp.getKd() > 0) {
                pp.setKd(pp.getKd() * (-1));
            }
            if (pp.getKi() > 0) {
                pp.setKi(pp.getKi() * (-1));
            }
        } else {
            if (pp.getKp() < 0) {
                pp.setKp(pp.getKp() * (-1));
            }
            if (pp.getKd() < 0) {
                pp.setKd(pp.getKd() * (-1));
            }
            if (pp.getKi() < 0) {
                pp.setKi(pp.getKi() * (-1));
            }
        }
        return pp;
    }


    public void setParameters(PidParameter parameters) {
        parameters = checkSigns(parameters);
        this.parameters = parameters;
    }
}