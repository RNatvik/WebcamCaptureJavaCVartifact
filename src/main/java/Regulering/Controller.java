package Regulering;


import data.*;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;
import pub_sub_service.Subscriber;

/**
 *
 */


public class Controller extends Subscriber implements Runnable, Publisher {

    private PID pidForward;
    private PID pidTurn;

    private RegulatorParameter regParam;
    private ControlInput manualControlInput;
    private double[] location; //x, y, radius, area

    private boolean newLocation;
    private boolean manualMode;
    private boolean newManualCommand;


    public Controller(Broker broker) {
        super(broker);

        this.pidForward = new PID(new PidParameter(0, 0, 0, 200, -200, 100, 0, 0, true));
        this.pidTurn = new PID(new PidParameter(0, 0, 0, 200, -200, 100, 0, 0, true));
        this.regParam = new RegulatorParameter(-20, -120, 20, 120, -200, 200, 1);
        this.location = new double[]{0, 0, 0, 0};
        this.newLocation = false;
        this.manualControlInput = new ControlInput(true, 0, 0);
        this.manualMode = this.manualControlInput.isManualControl();
        this.newManualCommand = true;

        this.getBroker().subscribeTo(Topic.PID_PARAM1, this);
        this.getBroker().subscribeTo(Topic.PID_PARAM2, this);
        this.getBroker().subscribeTo(Topic.REGULATOR_PARAM, this);
        this.getBroker().subscribeTo(Topic.IMPROC_DATA, this);
        this.getBroker().subscribeTo(Topic.CONTROLLER_INPUT, this);

    }

    @Override
    public void run() {
        this.readMessages();

        if (this.manualMode && this.newManualCommand) {
            double fw = this.manualControlInput.getForwardSpeed();
            double tr = this.manualControlInput.getTurnSpeed();
            // double[] motorSpeeds = calculateMotorSpeed(fw, tr);
            double leftSpeed = fw + tr;
            double rightSpeed = fw - tr;
            leftSpeed = clamp(leftSpeed, this.regParam.getMcMaximumReverse(), this.regParam.getMcMaximumForward());
            rightSpeed = clamp(rightSpeed, this.regParam.getMcMaximumReverse(), this.regParam.getMcMaximumForward());
            sendRegulatorOutput(new double[]{leftSpeed, rightSpeed});
            this.pidForward.reset();
            this.pidTurn.reset();
            this.newManualCommand = false;

        }

        if (this.newLocation && !this.manualMode) {
            if (this.location[3] > 0) {
                double x = this.location[0];
                //double y = this.location[1];
                double distance = this.location[2];
                //Function to linearize the radius to distance in cm
                //System.out.println("The radius is: " + radius);
                //double area = this.location[3];
                double[] pidOutputs = calculatePID(distance, x);
                double[] motorSpeeds = calculateMotorSpeed(pidOutputs[0], pidOutputs[1]);
                if (!(motorSpeeds[0] >= this.regParam.getMcMinimumForward() || motorSpeeds[0] <= this.regParam.getMcMinimumReverse())) {
                    motorSpeeds[0] = 0;
                }
                if (!(motorSpeeds[1] >= this.regParam.getMcMinimumForward() || motorSpeeds[1] <= this.regParam.getMcMinimumReverse())) {
                    motorSpeeds[1] = 0;
                }
                sendRegulatorOutput(motorSpeeds);
            } else {
                sendRegulatorOutput(new double[]{0, 0});
            }
            this.newLocation = false;
        }
    }


    /**
     * Calculate the pid outputs
     */
    private double[] calculatePID(double distance, double x) {
        double pidOut1 = this.pidForward.getOutput(distance);
        //System.out.println("PID FW output: " + pidOut1);
        double pidOut2 = this.pidTurn.getOutput(x);
//        pidOut1 = pidOut1 * this.regParam.getRatio();
//        pidOut2 = pidOut2 * (1 - this.regParam.getRatio());
        return new double[]{pidOut1, pidOut2};
    }

    /**
     * Calculate the motorspeeds based
     */
    private double[] calculateMotorSpeed(double forward, double turn) {
        double[] motorOutput = this.sumMotorVal(forward, turn);
        //System.out.println("Motoroutput after sum: "+motorOutput[0]+ " : " + motorOutput[1]);
        motorOutput[0] = (int) clamp(motorOutput[0], this.regParam.getMcMaximumReverse(), this.regParam.getMcMaximumForward());
        motorOutput[1] = (int) clamp(motorOutput[1], this.regParam.getMcMaximumReverse(), this.regParam.getMcMaximumForward());
        //System.out.println("After clamp: " +motorOutput[0]+ " : " + motorOutput[1]);
        // double[] mappedValues = this.mapMotorValue(motorOutput);
        //System.out.println("After map: " + mappedValues[0] + " : " + mappedValues[1]);
        return motorOutput;
    }

    /**
     *
     */
    private void sendRegulatorOutput(double[] motorsvalues) {
        Data outputData = new RegulatorOutput(motorsvalues[0], motorsvalues[1]);
        //System.out.println("Outputdata to string: " + outputData.toString());
        Message outputMessage = new Message(Topic.REGULATOR_OUTPUT, outputData);
        this.publish(this.getBroker(), outputMessage);
    }


    /**
     * Maps the output to appropriate values to send to the motors
     *
     * @param motors the motor values to map.
     * @return the motors values maped in desired range
     */
    private double[] mapMotorValue(double[] motors) {
        if (motors[0] < 0) {
            motors[0] = transformation(
                    this.regParam.getControllerMinOutput(),
                    0,
                    this.regParam.getMcMaximumReverse(),
                    this.regParam.getMcMinimumReverse(),
                    motors[0]
            );
        } else if (motors[0] > 0) {
            motors[0] = transformation(
                    0,
                    this.regParam.getControllerMaxOutput(),
                    this.regParam.getMcMinimumForward(),
                    this.regParam.getMcMaximumForward(),
                    motors[0]
            );
        } else {
            motors[0] = 0;
        }
        if (motors[1] < 0) {
            motors[1] = transformation(this.pidTurn.getParameters().getMinOutput(),
                    0,
                    this.regParam.getMcMaximumReverse(),
                    this.regParam.getMcMinimumReverse(),
                    motors[1]
            );
        } else if (motors[1] > 0) {
            motors[1] = transformation(0,
                    this.pidTurn.getParameters().getMaxOutput(),
                    this.regParam.getMcMinimumForward(),
                    this.regParam.getMcMaximumForward(),
                    motors[1]
            );
        } else {
            motors[1] = 0;
        }
        return motors;
    }


    /**
     * Transform one value from one range to another range
     *
     * @param a     lower input range
     * @param b     upper input range
     * @param c     lower output range
     * @param d     upper output range
     * @param input the number to transform, map
     * @return mappedValue
     */
    private double transformation(double a, double b, double c, double d, double input) {
        double mappedValue; // Value to return
        double value = (input - a) * ((d - c) / (b - a)) + c; //function
        mappedValue = (int) Math.round(value);
        return mappedValue;
    }


    /**
     * Function to sum up the outputs from the two PID
     * Sums up the output from the PID responsible for speed forward and
     * the PID responsible for the turning speed
     *
     * @param inputFW   speed forward
     * @param inputTurn speed turning
     * @return the summed motor values
     */
    private double[] sumMotorVal(double inputFW, double inputTurn) {
        //System.out.println("InputFW: " + inputFW);
        //System.out.println("Input turn: " + inputTurn);
        double leftMotor, rightMotor;
        if (inputTurn < 0) {
            leftMotor = inputFW + inputTurn;
            rightMotor = inputFW;
        } else if (inputTurn > 0) {
            leftMotor = inputFW;
            rightMotor = inputFW - inputTurn;
        } else {
            leftMotor = inputFW;
            rightMotor = inputFW;
        }
        return new double[]{leftMotor, rightMotor};
    }

    /**
     * Function to clamp a value within a range.
     *
     * @param val the value to clamp
     * @param min the minimum value of the val to clamo
     * @param max the maximum value of the val to clamp
     * @return the clamped value
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }


    @Override
    public void publish(Broker broker, Message message) {
        this.getBroker().addMessage(message);
    }

    @Override
    protected void doReadMessages() {
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            String topic = message.getTopic();
            Data data = message.getData();

            switch (topic) {
                case Topic.PID_PARAM1:
                    PidParameter pidParam1 = data.safeCast(PidParameter.class);
                    if (pidParam1 != null) {
                        this.pidForward.setParameters(pidParam1);
                        this.pidForward.reset();
                    }
                    break;

                case Topic.PID_PARAM2:
                    PidParameter pidParam2 = data.safeCast(PidParameter.class);
                    if (pidParam2 != null) {
                        this.pidTurn.setParameters(pidParam2);
                        this.pidTurn.reset();
                    }
                    break;

                case Topic.REGULATOR_PARAM:
                    RegulatorParameter param = data.safeCast(RegulatorParameter.class);
                    if (param != null) {
                        this.regParam = param;
                    }
                    break;

                case Topic.IMPROC_DATA:
                    ImageProcessorData imdata = data.safeCast(ImageProcessorData.class);
                    if (imdata != null) {
                        this.location = imdata.getLocation();
                        this.newLocation = true;
                    }
                    break;

                case Topic.CONTROLLER_INPUT:
                    ControlInput ci = data.safeCast(ControlInput.class);
                    if (ci != null) {
                        this.manualControlInput = ci;
                        this.manualMode = this.manualControlInput.isManualControl(); // Simpler
                        this.newManualCommand = true;
                    }

                default:
                    break;
            }

        }
    }
}
