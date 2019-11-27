from pyfirmata import Arduino, util
import time


print("hello")
# Setup the Arduino and define pins used
board = Arduino('/dev/ttyACM0')
print('Print details about arduino connected: ')
print(board)
max = 160
left = board.get_pin('d:10:s')
right = board.get_pin('d:11:s')
for i in range(max+1):
    x = 1 - (i/max)
    left.write(i)
    right.write(max-i)
    print(i)
    time.sleep(0.005)

for i in range(max+1):
    x = 1 - (i/max)
    left.write(max-i)
    right.write(i)
    print(i)
    time.sleep(0.005)

left.write(max)
right.write(0)

time.sleep(4)

left.write(0)
right.write(max)
