package Regulering;

import data.PidParameter;

/**
 * Taken from https://github.com/tekdemo/MiniPID-Java/blob/master/src/com/stormbots/MiniPID.java
 * and http://brettbeauregard.com/blog/2011/04/improving-the-beginners-pid-direction/improving-the-beginners-pid-introduction
 *
 * changed by LB.
 */

public class PID {

    //**********************************
    // Class private variables
    //**********************************

    private double F=0;

    private double maxIOutput=0;
    private double maxError=0;
    private double errorSum=0;

    private double lastActual=0;

    private boolean firstRun=true;
    private boolean reversed=false;

    private double outputRampRate=0;
    private double lastOutput=0;

    private double outputFilter=0;

    private double setpointRange=0;

    private PidParameter parameters;

    //**********************************
    // Constructor function
    //**********************************
    public PID(PidParameter parameters){
        this.parameters = parameters;
    }





    /**
     * Set the maximum output value contributed by the I component of the system
     * This can be used to prevent large windup issues and make tuning simpler
     * @param maximum Units are the same as the expected output value
     */
    public void setMaxIOutput(double maximum){
        // Internally maxError and Izone are similar, but scaled for different purposes.
        // The maxError is generated for simplifying math, since calculations against
        // the max error are far more common than changing the I term or Izone.
        maxIOutput=maximum;
        if(this.parameters.getKi()!=0){
            maxError=maxIOutput/this.parameters.getKi();
        }
    }


    /**
     * Set the operating direction of the PID controller
     * @param reversed Set true to reverse PID output
     */
    public void  setDirection(boolean reversed){
        this.reversed=reversed;
    }

    //**********************************
    // Primary operating functions
    //**********************************


    /**
     * Calculate the output value for the current PID cycle.<br>
     * @param actual The monitored value, typically as a sensor input.
     * @return calculated output value for driving the system
     */
    public double getOutput(double actual){
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

        // Ramp the setpoint used for calculations if user has opted to do so
        if(setpointRange!=0){
            setpoint=constrain(setpoint,actual-setpointRange,actual+setpointRange);
        }

        // Do the simple parts of the calculations
        double error=setpoint-actual;

        if (deadBand != 0){
            if(Math.abs(error) < deadBand){
                error = 0;
            }
        }

        // Calculate P term
        Poutput=P*error;

        // If this is our first time running this, we don't actually _have_ a previous input or output.
        // For sensor, sanely assume it was exactly where it is now.
        // For last output, we can assume it's the current time-independent outputs.
        if(firstRun){
            lastActual=actual;
            lastOutput=Poutput;
            firstRun=false;
        }

        // Calculate D Term
        // Note, this is negative. This actually "slows" the system if it's doing
        // the correct thing, and small values helps prevent output spikes and overshoot
        Doutput= -D*(actual-lastActual);
        lastActual=actual;

        // The Iterm is more complex. There's several things to factor in to make it easier to deal with.
        // 1. maxIoutput restricts the amount of output contributed by the Iterm.
        // 2. prevent windup by not increasing errorSum if we're already running against our max Ioutput
        // 3. prevent windup by not increasing errorSum if output is output=maxOutput
        Ioutput=I*errorSum;
        if(maxIOutput!=0){
            Ioutput=constrain(Ioutput,-maxIOutput,maxIOutput);
        }

        // And, finally, we can just add the terms up
        output= Poutput + Ioutput + Doutput;

        // Figure out what we're doing with the error.
        if(minOutput!=maxOutput && !bounded(output, minOutput,maxOutput) ){
            errorSum=error;
            // reset the error sum to a sane level
            // Setting to current error ensures a smooth transition when the P term
            // decreases enough for the I term to start acting upon the controller
            // From that point the I term will build up as would be expected
        }
        else if(outputRampRate!=0 && !bounded(output, lastOutput-outputRampRate,lastOutput+outputRampRate) ){
            errorSum=error;
        }
        else if(maxIOutput!=0){
            errorSum=constrain(errorSum+error,-maxError,maxError);
            // In addition to output limiting directly, we also want to prevent I term
            // buildup, so restrict the error directly
        }
        else{
            errorSum+=error;
        }

        // Restrict output to our specified output and ramp limits
        if(outputRampRate!=0){
            output=constrain(output, lastOutput-outputRampRate,lastOutput+outputRampRate);
        }
        if(minOutput!=maxOutput){
            output=constrain(output, minOutput,maxOutput);
        }
        if(outputFilter!=0){
            output=lastOutput*outputFilter+output*(1-outputFilter);
        }

        // Get a test printline with lots of details about the internal
        // calculations. This can be useful for debugging.
        // //System.out.printf("Final output %5.2f [ %5.2f, %5.2f , %5.2f  ], eSum %.2f\n",output,Poutput, Ioutput, Doutput,errorSum );
        // //System.out.printf("%5.2f\t%5.2f\t%5.2f\t%5.2f\n",output,Poutput, Ioutput, Doutput );

        lastOutput=output;
        return output;
    }



    /**
     * Resets the controller. This erases the I term buildup, and removes
     * D gain on the next loop.<br>
     * This should be used any time the PID is disabled or inactive for extended
     * duration, and the controlled portion of the system may have changed due to
     * external forces.
     */
    public void reset(){
        firstRun=true;
        errorSum=0;
    }

    /**
     * Set the maximum rate the output can increase per cycle.<br>
     * This can prevent sharp jumps in output when changing setpoints or
     * enabling a PID system, which might cause stress on physical or electrical
     * systems.  <br>
     * Can be very useful for fast-reacting control loops, such as ones
     * with large P or D values and feed-forward systems.
     *
     * @param rate, with units being the same as the output
     */
    public void setOutputRampRate(double rate){
        outputRampRate=rate;
    }

    /**
     * Set a limit on how far the setpoint can be from the current position
     * <br>Can simplify tuning by helping tuning over a small range applies to a much larger range.
     * <br>This limits the reactivity of P term, and restricts impact of large D term
     * during large setpoint adjustments. Increases lag and I term if range is too small.
     * @param range, with units being the same as the expected sensor range.
     */
    public void setSetpointRange(double range){
        setpointRange=range;
    }

    /**
     * Set a filter on the output to reduce sharp oscillations. <br>
     * 0.1 is likely a sane starting value. Larger values use historical data
     * more heavily, with low values weigh newer data. 0 will disable, filtering, and use
     * only the most recent value. <br>
     * Increasing the filter strength will P and D oscillations, but force larger I
     * values and increase I term overshoot.<br>
     * Uses an exponential wieghted rolling sum filter, according to a simple <br>
     * <pre>output*(1-strength)*sum(0..n){output*strength^n}</pre> algorithm.
     * @param strength valid between [0..1), meaning [current output only.. historical output only)
     */
    public void setOutputFilter(double strength){
        if(strength==0 || bounded(strength,0,1)){
            outputFilter=strength;
        }
    }

    //**************************************
    // Helper functions
    //**************************************

    /**
     * Forces a value into a specific range
     * @param value input value
     * @param min maximum returned value
     * @param max minimum value in range
     * @return Value if it's within provided range, min or max otherwise
     */
    private double constrain(double value, double min, double max){
        if(value > max){ return max;}
        if(value < min){ return min;}
        return value;
    }

    /**
     * Test if the value is within the min and max, inclusive
     * @param value to test
     * @param min Minimum value of range
     * @param max Maximum value of range
     * @return true if value is within range, false otherwise
     */
    private boolean bounded(double value, double min, double max){
        // Note, this is an inclusive range. This is so tests like
        // `bounded(constrain(0,0,1),0,1)` will return false.
        // This is more helpful for determining edge-case behaviour
        // than <= is.
        return (min<value) && (value<max);
    }

    /**
     * To operate correctly, all PID parameters require the same sign
     * This should align with the {@literal}reversed value
     */
    private PidParameter checkSigns(PidParameter pp){
        if (pp.isReversed()){
            if(pp.getKp()>0){
                pp.setKp(pp.getKp()*(-1));
            }
            if(pp.getKd()>0){
                pp.setKd(pp.getKd()*(-1));
            }
            if(pp.getKi()>0){
                pp.setKi(pp.getKi()*(-1));
            }
        }
        else{
            if(pp.getKp()<0){
                pp.setKp(pp.getKp()*(-1));
            }
            if(pp.getKd()<0){
                pp.setKd(pp.getKd()*(-1));
            }
            if(pp.getKi()<0){
                pp.setKi(pp.getKi()*(-1));
            }
        }
        return pp;
    }

    public PidParameter getParameters() {
        return parameters;
    }

    public void setParameters(PidParameter parameters) {
        parameters = checkSigns(parameters);
        this.parameters = parameters;
    }
}