package Regulering;

import java.util.TimerTask;

/**
 * Testing timer based controller
 *
 *
 */


public class Controller extends TimerTask {

    private PID pidForward;
    private PID pidTurn;
    private StorageBox sb;
    private double setPoint = 100;

    private double pidOutputMax = 200;
    private double pidOutputMin = -200;

    private double mcMinimumReverse = -20;
    private double mcMaximumReverse = -100;
    private double mcMinimumForward = 20;
    private double mcMaximumForward = 100;


    public Controller(StorageBox sb){
        this.sb = sb;

        pidForward = new PID(5,0,0.00001);
        pidForward.setOutputLimits(pidOutputMin,pidOutputMax);

        pidTurn = new PID(1,0,0.000001);
        pidTurn.setOutputLimits(pidOutputMin,pidOutputMax);


    }

    @Override
    public void run() {
        // consumer will always ask for values from storage box
        System.out.println("Controller requests for value: ");
        synchronized(sb) {   // storageBox is the shared resource (critical) and must be synchronized
            if (sb.getAvailable()) { // Conditionally read value
                double value = sb.getValue();
                System.out.println("Controller  got: " + value);



                double pidForwardOutput = pidForward.getOutput(50,setPoint);
                //double pidForwardOutput = -10;
                System.out.println("pid forward: "+ pidForwardOutput );
                double pidTurnOutput = pidTurn.getOutput(0,0);
                //double pidTurnOutput = 10;
                System.out.println("pid turn: "+ pidTurnOutput );

                double [] motorOutput = sumPID(pidForwardOutput, pidTurnOutput);

                System.out.println("After sumPid: " + motorOutput[0] + " : " + motorOutput[1]);

                motorOutput[0] = clamp(motorOutput[0],pidOutputMin,pidOutputMax);
                motorOutput[1] = clamp(motorOutput[1],pidOutputMin,pidOutputMax);

                System.out.println("After clamping: " + motorOutput[0] + " : " + motorOutput[1]);

                motorOutput = mapMotorValue(motorOutput);

                System.out.println("Controller output left motor: " + motorOutput[0]);
                System.out.println("Controller output right motor: " + motorOutput[1]);
            }
        }
    }


    /**
     * Maps the output from the pid to approtiete values to send to the motors
     * @param motors the motor values to map.
     * @return the motors values maped in desired range
     */
    private double[] mapMotorValue(double [] motors){
        if (motors[0] < 0){
            motors[0] = transformation(pidOutputMin,0,mcMaximumReverse,mcMinimumReverse,motors[0]);
        }
        else if(motors[0] > 0){
            motors[0] = transformation(0,pidOutputMax,mcMinimumForward,mcMaximumForward,motors[0]);
        }
        else {
            motors[0] = 0;
        }
        if (motors[1] < 0){
            motors[1] = transformation(pidOutputMin,0,mcMaximumReverse,mcMinimumReverse,motors[1]);
        }
        else if(motors[1] > 0){
            motors[1] = transformation(0,pidOutputMax,mcMinimumForward,mcMaximumForward,motors[1]);
        }
        else {
            motors[1] = 0;
        }
        return motors;
    }




    /**
     * Transform one value from one range to another range
     * @param a lower input range
     * @param b upper input range
     * @param c lower output range
     * @param d upper output range
     * @param input the number to transform, map
     * @return mappedValue
     */
    private double transformation(double a,double b, double c, double d, double input ){
        double mappedValue = 0; // Value to return
        double value = (input-a)*((d-c)/(b-a)) + c; //function
        mappedValue = (int) Math.round(value);
        return mappedValue;
    }


    /**
     * Function to sum up the outputs from the two PID
     *  Sums up the output from the PID responsible for speed forward and
     *  the PID responsible for the turning speed
     * @param inputFW speed forward
     * @param inputTurn speed turning
     * @return
     */
    private double[] sumPID(double inputFW,double inputTurn){
        double leftMotor = 0,rightMotor = 0;
        if (inputTurn < 0){
            leftMotor = inputFW - inputTurn;
            rightMotor = inputFW + inputTurn;
        }
        else if (inputTurn > 0){
            leftMotor = inputFW + inputTurn;
            rightMotor = inputFW - inputTurn;
        }
        else{
            leftMotor = inputFW;
            rightMotor = inputFW;
        }
        double [] motorValues = {leftMotor, rightMotor};
        return motorValues;
    }

    /**
     * Function to clamp a value within a range.
     * @param val the value to clamp
     * @param min the minimum value of the val to clamo
     * @param max the maximum value of the val to clamp
     * @return the clamped value
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }



}
