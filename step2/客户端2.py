#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import socket
import sys
Relay_Ch2 = 20
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(Relay_Ch2, GPIO.OUT)


def start():
    GPIO.output(Relay_Ch2, GPIO.HIGH)
    while(1):
        try:
            recvSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            recvSock.connect(('120.24.12.197', 9901))
            buf = recvSock.recv(1024)
            if(buf == None):
                continue
            if(buf == 'open'):
                GPIO.output(Relay_Ch2, GPIO.LOW)
            elif(buf == 'close'):
                GPIO.output(Relay_Ch2, GPIO.HIGH)
            else:
                None
            recvSock.close()
        except Exception, e:
            print 'error:' + str(e)
            time.sleep(1)
if __name__ == '__main__':
    if(sys.argv[1] == "start"):
        start()
