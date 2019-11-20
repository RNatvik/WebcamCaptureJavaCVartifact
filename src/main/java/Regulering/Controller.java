package Regulering;


import data.*;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;
import pub_sub_service.Subscriber;

/**
 * This class is an controller for controlling the speed of the car based on input
 * In Tracking mode the speed of the motors are calculated by using two PID, one for forward speed and one for
 * turning correction.
 * In Manual mode the speed of the motors are set by the GUI, the speed is updated when the it receives a new command
 * from the broker.
 *
 * When its run() method is invoked it is designed to run one time. The class is indented to be run by a
 * scheduled executor service.
 * It needs an instance of the broker, that is shared with the rest of the system.
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

    private long testTime;


    /**
     * Constructor for the controller class
     * @param broker the broker to receive and send data to
     */
    public Controller(Broker broker) {
        super(broker);
        this.testTime = 0;
        this.pidForward = new PID(new PidParameter(0, 0, 0, 200, -200, 100, 0, 0, true));
        this.pidTurn = new PID(new PidParameter(0, 0, 0, 200, -200, 100, 0, 0, true));
        this.regParam = new RegulatorParameter(-20, -120, 20, 120, -200, 200, 1);
        this.location = new double[]{0, 0, 0, 0};
        this.newLocation = false;
        this.manualControlInput = new ControlInput(true, 0, 0);
        this.manualMode = this.manualControlInput.isManualControl();
        this.newManualCommand = true;

        // Subscribe to the desired topics
        this.getBroker().subscribeTo(Topic.PID_PARAM1, this);
        this.getBroker().subscribeTo(Topic.PID_PARAM2, this);
        this.getBroker().subscribeTo(Topic.REGULATOR_PARAM, this);
        this.getBroker().subscribeTo(Topic.IMPROC_DATA, this);
        this.getBroker().subscribeTo(Topic.CONTROLLER_INPUT, this);

    }

    /**
     * Controllers main loop
     */
    @Override
    public void run() {
        this.readMessages(); //Check if new messages available
        long startTime = System.currentTimeMillis();
        if (this.manualMode && this.newManualCommand) {
            double fw = this.manualControlInput.getForwardSpeed();
            double tr = this.manualControlInput.getTurnSpeed();
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
        long endTime = System.currentTimeMillis();
        this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                String.format("%s run time: %s\n%s dt: %s",
                        this, (endTime-startTime), this, (startTime-testTime)
                )
        )));
        this.testTime = startTime;
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
     * Calculate the motor speeds when given a speed forward and a turn correction.
     * Clamps the motor speeds within a desired range.
     * @param forward the desired speed forward
     * @param turn the desired turn correction speed
     * @return motorOutput, the motor speeds as an array
     */
    private double[] calculateMotorSpeed(double forward, double turn) {
        double[] motorOutput = this.sumMotorVal(forward, turn);
        motorOutput[0] = (int) clamp(motorOutput[0], this.regParam.getMcMaximumReverse(), this.regParam.getMcMaximumForward());
        motorOutput[1] = (int) clamp(motorOutput[1], this.regParam.getMcMaximumReverse(), this.regParam.getMcMaximumForward());
        return motorOutput;
    }

    /**
     * Send the regulator output to the broker, based on the input values.
     * @param motorsvalues the values to to send to motors
     */
    private void sendRegulatorOutput(double[] motorsvalues) {
        Data outputData = new RegulatorOutput(motorsvalues[0], motorsvalues[1]);
        Message outputMessage = new Message(Topic.REGULATOR_OUTPUT, outputData);
        this.publish(this.getBroker(), outputMessage);
    }


    /**
     * Function to sum up the outputs from the given input speeds
     *
     * @param inputFW  speed forward
     * @param inputTurn speed turning correction
     * @return the summed motor values
     */
    private double[] sumMotorVal(double inputFW, double inputTurn) {
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
     * @param min the minimum value of the val to clamp
     * @param max the maximum value of the val to clamp
     * @return the clamped value
     */
    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }


    /**
     * Publish the message to the broker.
     * @param broker the message broker to publish to
     * @param message the message to publish
     */
    @Override
    public void publish(Broker broker, Message message) {
        this.getBroker().addMessage(message);
    }

    /**
     * A handler for reading incoming messages
     */
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
                        this.manualMode = this.manualControlInput.isManualControl();
                        this.newManualCommand = true;
                    }

                default:
                    break;
            }

        }
    }
}
