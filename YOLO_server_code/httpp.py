import requests, json
import threading, time
import datetime
def comm(n=3):
    url1 = "http://223.131.2.220:1818/py/parkin"
    url2 = "http://223.131.2.220:1818/py/parkout"
    url3 = "http://223.131.2.220:1818/py/accident"
    re_car_info = str(None)
    re_car_status = str(None)
    re_seat_num = str(None)
    re_car_num1 = str(None)
    re_car_num2 = str(None)
    re_ac_time = str(None)
    parkinfo = []
    accident = []
    accident_string = str(None)
    while True:
        f1 = open('C:/Users/Kim/repos/yolov3_deepsort/parkinglot.txt', 'r')
        f2 = open('C:/Users/Kim/repos/yolov3_deepsort/accident.txt', 'r')
        tempFile1 = f1.read().split()
        tempFile2 = f2.read().split() 
        for i in tempFile1:
            parkinfo = tempFile1
        for i in tempFile2:
            accident = tempFile2
        f1.close()
        f2.close()
        if len(accident) == 4:
            accident_string = accident[2] + accident[3]
            accident_string = datetime.datetime.strptime(accident_string, '%Y-%m-%d%H:%M:%S.%f')
            accident_string = accident_string.strftime('%Y-%m-%d %H:%M:%S')
        if re_car_info == parkinfo[0] and re_seat_num == parkinfo[1] and re_car_status == parkinfo[2]: #차량 입차,주차,출차
            pass
        elif parkinfo[0] != str(None) and parkinfo[1] == str(None) and parkinfo[2] == str(None):
            pass
        elif re_car_info != parkinfo[0] and re_seat_num != parkinfo[1] or re_car_status != parkinfo[2]:
            print(parkinfo)
            if parkinfo[2] == 'parkIn':
                re_car_info = parkinfo[0]
                re_seat_num = parkinfo[1]
                re_car_status = parkinfo[2]
                cars1 = {'Content-Type': 'application/json; charset=utf-8'}
                param1 = {
                  'carNum' : parkinfo[0],
                  'seatNum' : int(parkinfo[1]),
                  'status' : parkinfo[2]
                }
                respone1 = requests.post(url1, headers=cars1, data=json.dumps(param1))
                print(respone1)
            elif parkinfo[2] == 'parkOut':
                re_car_info = parkinfo[0]
                re_seat_num = parkinfo[1]
                re_car_status = parkinfo[2]
                cars2 = {'Content-Type': 'application/json; charset=utf-8'}
                param2 = {
                  'carNum' : parkinfo[0],
                  'seatNum' : int(parkinfo[1]),
                  'status' : parkinfo[2]
                }
                respone2 = requests.post(url2, headers=cars1, data=json.dumps(param2))
                print(respone2)
        if re_car_num1 == accident[0] and re_car_num2 == accident[1] and re_ac_time == accident_string: #차량 사고판단
            continue
        elif re_car_num1 != accident[0] or re_car_num2 != accident[1] or re_ac_time != accident_string:
            print(accident_string)
            re_car_num1 = accident[0]
            re_car_num2 = accident[1]
            re_ac_time = accident_string
            def myconverter(o):
                    if isinstance(o, datetime.datetime):
                        return o._str_()
            acc = {'Content-Type': 'application/json; charset=utf-8'}
            param3 = {
                  'carNum1' : accident[0],
                  'carNum2' : accident[1],
            }
            param3['accTime'] = accident_string
            respone3 = requests.post(url3, headers=acc, data=json.dumps(param3, default = myconverter))
        time.sleep(n)


thread = threading.Thread(target=comm)
thread.start()

