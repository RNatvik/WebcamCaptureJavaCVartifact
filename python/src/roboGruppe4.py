import socket
import json
from src.roboclaw_3 import Roboclaw
from pyfirmata import Arduino, util


def control_speed(mc, adr, speed_m1, speed_m2):
    # speedM1 = leftMotorSpeed, speedM2 = rightMotorSpeed
    if speed_m1 > 0:
        mc.ForwardM1(adr, speed_m1)
    elif speed_m1 < 0:
        speed_m1 = speed_m1 * (-1)
        mc.BackwardM1(adr, speed_m1)
    else:
        mc.ForwardM1(adr, 0)

    if speed_m2 > 0:
        mc.ForwardM2(adr, speed_m2)
    elif speed_m2 < 0:
        speed_m2 = speed_m2 * (-1)
        mc.BackwardM2(adr, speed_m2)
    else:
        mc.ForwardM2(adr, 0)


def control_gripper(command, gripper_left, gripper_right):
    if command is True:
        gripper_left.write(0)
        gripper_right.write(160)
    else:
        gripper_left.write(160)
        gripper_right.write(0)

def main():
    # Setup the Roboclaw
    rc = Roboclaw("/dev/ttyACM1", 115200)  # Linux comport name
    address = 0x80
    rc.Open()
    print('Details about the Robobclaw: ')
    version = rc.ReadVersion(address)
    if not version[0]:
        print("GETVERSION Failed")
        exit()
    else:
        print(repr(version[1]))
        print("Car main battery voltage at start of script: ", rc.ReadMainBatteryVoltage(address))


    # Setup the Arduino and define pins used
    board = Arduino('/dev/ttyACM0')
    print('Print details about arduino connected: ')
    print(board)
    gripperRight = board.get_pin('d:11:s')
    gripperLeft = board.get_pin('d:10:s')

    # Setup the tcp com to the server.
    TCP_IP = '192.168.0.50'
    TCP_PORT = 9876
    BUFFER_SIZE = 1024
    subStringREG = 'SUB::REG_OUTPUT\n'
    subStringGripper = 'SUB::GRIPPER_COMMANDS\n'

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as soc:
        soc.connect((TCP_IP, TCP_PORT))
        soc.settimeout(None)
        soc.sendall(subStringREG.encode('utf-8'))
        soc.sendall(subStringGripper.encode('utf-8'))

        loop = True  # exit condition
        while loop:

            message = soc.recv(BUFFER_SIZE)
            if message is b'':
                loop = False
                print('Received: b'' and stopped the loop')
                break

            try:
                msg = json.loads(message)
                topic = msg['topic']
                if str(topic).__eq__('GRIPPER_COMMANDS'):
                    data = msg['data']
                    gripper = data['command']
                    control_gripper(gripper,gripperLeft,gripperRight)
                if str(topic).__eq__('REG_OUTPUT'):
                    data = msg['data']
                    rightMotorSpeed = data['rightMotor']
                    leftMotorSpeed = data['leftMotor']
                    control_speed(rc, address, leftMotorSpeed, rightMotorSpeed)
            except json.decoder.JSONDecodeError:
                print("json.decoder.JSONDecodeError")
                pass

    rc.ForwardM1(address, 0)
    rc.ForwardM2(address, 0)
    soc.close()


if __name__ == '__main__':
    main()

