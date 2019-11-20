package data;

/**
 * Constant list for message topics used in the pub sub service.
 */
public abstract class Topic {

    public static final String OUTPUT_IMAGE = "OUTPUT_IMAGE";
    public static final String IMPROC_DATA = "IMPROC_DATA";
    public static final String IMPROC_PARAM = "IMPROC_PARAM";
    public static final String PID_PARAM1 = "PID_PARAM1";
    public static final String PID_PARAM2 = "PID_PARAM2";
    public static final String REGULATOR_PARAM = "REG_PARAM";
    public static final String REGULATOR_OUTPUT = "REG_OUTPUT";
    public static final String CONTROLLER_INPUT = "CONTROLLER_INPUT";
    public static final String CONSOLE_OUTPUT = "CONSOLE_OUTPUT";
    public static final String DEBUG_OUTPUT = "DEBUG_OUTPUT";
    public static final String GRIPPER = "GRIPPER_COMMANDS";

}
