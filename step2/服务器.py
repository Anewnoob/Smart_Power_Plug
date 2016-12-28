#!/usr/bin/env python
#coding:utf8
import random,threading,time,socket
def start():
    while(True):
        try:
            al_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            al_sock.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
            al_sock.bind(('0.0.0.0', 8801))
            while(True):
                al_sock.listen(5)
                print 'listen android'
                android_connection, address = al_sock.accept()
                print 'android connected'
                android_connection.settimeout(1024)
                buf = android_connection.recv(1024)
                print 'android recv:' + buf
                try:
                    print 'in try'
                    sl_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    sl_sock.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
                    sl_sock.bind(('0.0.0.0', 9901))
                    sl_sock.listen(10)
                    sl_sock.settimeout(3)
                    print 'listen socket'
                    socket_connection, address = sl_sock.accept()
                    print 'socket connected:'
                    socket_connection.send(buf)
                    socket_connection.close()
                except Exception as e:
                    print 'My exception:' + str(e)
                    android_connection.send('error')
                else: 
                    android_connection.send('okay')
                finally:
                    android_connection.close()
                    time.sleep(1)
        except Exception as e:
            print 'outer exception:' + str(e)
            time.sleep(1)
if __name__ == '__main__':
    start()
