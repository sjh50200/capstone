import random
import time
import paho.mqtt.client as mqtt_client


# broker 정보 #1
broker_address = "test.mosquitto.org"
broker_port = 1883

topic = "/accident"


def connect_mqtt() -> mqtt_client:
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT Broker")
        else:
            print(f"Failed to connect, Returned code: {rc}")

    def on_disconnect(client, userdata, flags, rc=0):
        print(f"disconnected result code {str(rc)}")

    def on_log(client, userdata, level, buf):
        print(f"log: {buf}")

    # client 생성 #2
    client_id = f"mqtt_client_{random.randint(0, 1000)}"
    client = mqtt_client.Client(client_id)

    # 콜백 함수 설정 #3
    client.on_connect = on_connect
    client.on_disconnect = on_disconnect
    client.on_log = on_log

    # broker 연결 #4
    client.connect(host=broker_address, port=broker_port)
    return client


def publish(client: mqtt_client):
    msg_count = 0
    info = []
    cpin = [str(None), str(None)]
    while True:
        time.sleep(1)
        f = open('C:/Users/Kim/repos/yolov3_deepsort/accident.txt', 'r')
        tff = f.read().split()
        for i in tff:
            info = tff
        f.close()
        if info[0] != str(None) and info[1] != str(None):
            if info[0] != cpin[0] and info[1] != cpin[1]:
                cpin[0] = info[0]
                cpin[1] = info[1]
                msg = info[0] + " " + info[1]
                result = client.publish(topic, msg)
            elif info[0] == cpin[0] and info[1] == cpin[1]:
                continue
        # result: [0, 1]
            status = result[0]
            if status == 0:
                print(f"Send `{msg}` to topic `{topic}`")
            else:
                print(f"Failed to send message to topic {topic}")
            msg_count += 1
        elif info[0] == str(None) or info[1] == str(None):
            continue


def run():
    client = connect_mqtt()
    client.loop_start() #5
    print(f"connect to broker {broker_address}:{broker_port}")
    publish(client) #6

if __name__ == '__main__':
    run()