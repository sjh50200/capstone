import requests, json
import threading, time
import datetime
import sys
sys.path.append("..")
import time
import paho.mqtt.client as mqtt

client = mqtt.Client("parking")
client.connect("test.mosquitto.org", 1883)
carNum='1234'
url1 = "http://223.131.2.220:1818/raspberry/entry"
url2 = "http://223.131.2.220:1818/raspberry/exit"
time=datetime.datetime.now()
time=time.strftime('%Y-%m-%d %H:%M:%S')
def myconverter(o):
    if isinstance(o,datetime.datetime):
        return o._str_()
car={'Content-Type':'application/json; charset=utf-8'}
param={
    'carNum' : '1234',
    'time' : time
}
def mqttsending():
	client.publish("parking", carNum)
	print("mqtt publish %s to Yolov3" % carNum)
def httpInSending():
  response=requests.post(url1,headers=car,data=json.dumps(param,default=myconverter))
  print("http sending %s to webserver" % carNum)
  print(response)
  print(time)
def httpOutSending():
  response=requests.post(url2,headers=car,data=json.dumps(param,default=myconverter))
  print("Car %s Exit"%carNum)
  print("http sending %s to wepserver" % carNum)
  print(response)
  print(time)
if __name__=="__main__":
  mqttsending()
  #httpInSending()
  httpOutSending()
