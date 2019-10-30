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
    private int[] location; //x, y, radius, area

    private boolean newLocation;
    private boolean manualMode;
    private boolean newManualCommand;


    public Controller(Broker broker) {
        super(broker);

        this.pidForward = new PID(new PidParameter(10,0,0,200,-200, 100));
        this.pidTurn = new PID(new PidParameter(0,0,0,200,-200, 100));
        this.regParam = new RegulatorParameter(-20, -100, 20, 100,-200,200);
        this.location = new int[]{0,0,0,0};
        this.newLocation = false;
        this.manualControlInput = new ControlInput(true,0,0);
        this.manualMode = this.manualControlInput.isManualControl();
        this.newManualCommand = true;

        this.getBroker().subscribeTo(Topic.PID_PARAM1, this);
        this.getBroker().subscribeTo(Topic.PID_PARAM2, this);
        this.getBroker().subscribeTo(Topic.REGULATOR_PARAM, this);
        this.getBroker().subscribeTo(Topic.IMAGE_DATA, this);
        this.getBroker().subscribeTo(Topic.CONTROLER_INPUT,this);

    }

    @Override
    public void run() {
        this.readMessages();

        if (this.manualMode && this.newManualCommand) {
            calculateMotorSpeed(this.manualControlInput.getForwardSpeed(),this.manualControlInput.getTurnSpeed());
            this.pidForward.reset();
            this.pidTurn.reset();
            this.newManualCommand = false;
        }

        if( this.newLocation && !this.manualMode )
        {
            double[] pidOutputs = calculatePID();
            calculateMotorSpeed(pidOutputs[0],pidOutputs[1]);
            this.newLocation = false;
        }
    }

    /**
     * Calculate the pid outputs
     */
    private double[] calculatePID() {
        int x = this.location[0];
        int y = this.location[1];
        int radius = this.location[2];
        int area = this.location[3];

        double pidOut1 = this.pidForward.getOutput(radius);
        double pidOut2 = this.pidTurn.getOutput(x);

        double[] pidOutputs = {pidOut1,pidOut2};
        return pidOutputs;
    }

    /**
     * Calculate the motorspeeds based
     */
    private void calculateMotorSpeed(double forward,double turn) {
        double[] motorOutput = this.sumMotorVal(forward, turn);
        motorOutput[0] = clamp(motorOutput[0], this.regParam.getControllerMinOutput(),this.regParam.getControllerMaxOutput());
        motorOutput[1] = clamp(motorOutput[1],this.regParam.getControllerMinOutput(),this.regParam.getControllerMaxOutput());
        double[] mappedValues = this.mapMotorValue(motorOutput);

        Data outputData = new RegulatorOutput(mappedValues[0], mappedValues[1]);
        Message outputMessage = new Message(Topic.REGULATOR_OUTPUT, outputData);
        this.publish(this.getBroker(), outputMessage);
    }


    /**
     * Maps the output from the pid to appropriate values to send to the motors
     *
     * @param motors the motor values to map.
     * @return the motors values maped in desired range
     */
    private double[] mapMotorValue(double[] motors) {
        if (motors[0] < 0) {
            motors[0] = transformation(this.pidForward.getParameters().getMinOutput(),
                    0,
                    this.regParam.getMcMaximumReverse(),
                    this.regParam.getMcMinimumReverse(),
                    motors[0]
            );
        } else if (motors[0] > 0) {
            motors[0] = transformation(0,
                    this.pidForward.getParameters().getMaxOutput(),
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
        double mappedValue = 0; // Value to return
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
     * @return
     */
    private double[] sumMotorVal(double inputFW, double inputTurn) {
        double leftMotor = 0, rightMotor = 0;
        if (inputTurn < 0) {
            leftMotor = inputFW - inputTurn;
            rightMotor = inputFW + inputTurn;
        } else if (inputTurn > 0) {
            leftMotor = inputFW + inputTurn;
            rightMotor = inputFW - inputTurn;
        } else {
            leftMotor = inputFW;
            rightMotor = inputFW;
        }
        double[] motorValues = {leftMotor, rightMotor};
        return motorValues;
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
                        //TODO: Maybe add reset
                    }
                    break;

                case Topic.PID_PARAM2:
                    PidParameter pidParam2 = data.safeCast(PidParameter.class);
                    if (pidParam2 != null) {
                        this.pidTurn.setParameters(pidParam2);
                        //TODO: Maybe add reset
                    }
                    break;

                case Topic.REGULATOR_PARAM:
                    RegulatorParameter param = data.safeCast(RegulatorParameter.class);
                    if (param != null) {
                        this.regParam = param;
                    }
                    break;

                case Topic.IMAGE_DATA:
                    ImageProcessorData imdata = data.safeCast(ImageProcessorData.class);
                    if (imdata != null) {
                        this.location = imdata.getLocation();
                        this.newLocation = true;
                    }
                    break;

                case Topic.CONTROLER_INPUT:
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
