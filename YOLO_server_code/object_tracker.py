import time, random
import threading
import numpy as np
from absl import app, flags, logging
from absl.flags import FLAGS
import cv2
import matplotlib.pyplot as plt
import tensorflow as tf
from yolov3_tf2.models import (
    YoloV3, YoloV3Tiny
)
from yolov3_tf2.dataset import transform_images
from yolov3_tf2.utils import draw_outputs, convert_boxes

from deep_sort import preprocessing
from deep_sort import nn_matching
from deep_sort.detection import Detection
from deep_sort.tracker import Tracker
from tools import generate_detections as gdet
from PIL import Image
import datetime

flags.DEFINE_string('classes', './data/labels/coco.names', 'path to classes file')
flags.DEFINE_string('weights', './weights/yolov3.tf',
                    'path to weights file')
flags.DEFINE_boolean('tiny', False, 'yolov3 or yolov3-tiny')
flags.DEFINE_integer('size', 416, 'resize images to')
flags.DEFINE_string('video', './data/video/test.mp4',
                    'path to video file or number for webcam)')
flags.DEFINE_string('output', None, 'path to output video')
flags.DEFINE_string('output_format', 'XVID', 'codec used in VideoWriter when saving video to file')
flags.DEFINE_integer('num_classes', 80, 'number of classes in the model')
time_a = int(0)
time_b = int(0)
time_c = int(0)
time_d = int(0)

def fun_a(): #1번자리에 대한 타이머
    global time_a
    time_a += 1
    timer=threading.Timer(10,fun_a)
    timer.start()
    if time_a == 3:
        timer.cancel()

def fun_b(): #2번자리에 대한 타이머
    global time_b
    time_b += 1
    timer=threading.Timer(10,fun_b)
    timer.start()
    if time_b == 3:
        timer.cancel()

def fun_c(): #3번자리에 대한 타이머
    global time_c
    time_c += 1
    timer=threading.Timer(10,fun_c)
    timer.start()
    if time_c == 3:
        timer.cancel()

def fun_d(): #4번자리에 대한 타이머
    global time_d
    time_d += 1
    timer=threading.Timer(10,fun_d)
    timer.start()
    if time_d == 3:
        timer.cancel()

def main(_argv):
    # Definition of the parameters
    max_cosine_distance = 0.5
    nn_budget = None
    nms_max_overlap = 1.0
    
    parking_before = int(0)
    parking_after = int(0)
    parking1 = int(0)
    parking2 = int(0)
    parking3 = int(0)
    parking4 = int(0)
    parking = [[None,None],[None,None]]
    carn = [str(None)]*4
    co = int(0)
    ac_num = int(0)
    accident_time = None
    ccc = int(0) #mqtt
    
    #initialize deep sort
    model_filename = 'model_data/mars-small128.pb'
    encoder = gdet.create_box_encoder(model_filename, batch_size=1)
    metric = nn_matching.NearestNeighborDistanceMetric("cosine", max_cosine_distance, nn_budget)
    tracker = Tracker(metric)

    physical_devices = tf.config.experimental.list_physical_devices('GPU')
    if len(physical_devices) > 0:
        tf.config.experimental.set_memory_growth(physical_devices[0], True)

    if FLAGS.tiny:
        yolo = YoloV3Tiny(classes=FLAGS.num_classes)
    else:
        yolo = YoloV3(classes=FLAGS.num_classes)

    yolo.load_weights(FLAGS.weights)
    logging.info('weights loaded')

    class_names = [c.strip() for c in open(FLAGS.classes).readlines()]
    logging.info('classes loaded')

    try:
        vid = cv2.VideoCapture(int(FLAGS.video))
    except:
        vid = cv2.VideoCapture(FLAGS.video)

    out = None

    if FLAGS.output:
        # by default VideoCapture returns float instead of int
        width = int(vid.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(vid.get(cv2.CAP_PROP_FRAME_HEIGHT))
        fps = int(vid.get(cv2.CAP_PROP_FPS))
        codec = cv2.VideoWriter_fourcc(*FLAGS.output_format)
        out = cv2.VideoWriter(FLAGS.output, codec, fps, (width, height))
        list_file = open('detection.txt', 'w')
        frame_index = -1 
    
    fps = 0.0
    count = 0
    while True:
        _, img = vid.read()

        if img is None:
            logging.warning("Empty Frame")
            time.sleep(0.1)
            count+=1
            if count < 3:
                continue
            else: 
                break

        img_in = cv2.cvtColor(img, cv2.COLOR_BGR2RGB) 
        img_in = tf.expand_dims(img_in, 0)
        img_in = transform_images(img_in, FLAGS.size)

        t1 = time.time()
        boxes, scores, classes, nums = yolo.predict(img_in)
        classes = classes[0]
        names = []
        for i in range(len(classes)):
            names.append(class_names[int(classes[i])])
        names = np.array(names)
        converted_boxes = convert_boxes(img, boxes[0])
        features = encoder(img, converted_boxes)    
        detections = [Detection(bbox, score, class_name, feature) for bbox, score, class_name, feature in zip(converted_boxes, scores[0], names, features)]
        
        #initialize color map
        cmap = plt.get_cmap('tab20b')
        colors = [cmap(i)[:3] for i in np.linspace(0, 1, 20)]

        # run non-maxima suppresion
        boxs = np.array([d.tlwh for d in detections])
        scores = np.array([d.confidence for d in detections])
        classes = np.array([d.class_name for d in detections])
        indices = preprocessing.non_max_suppression(boxs, classes, nms_max_overlap, scores)
        detections = [detections[i] for i in indices]        

        # Call the tracker
        tracker.predict()
        tracker.update(detections)
        
        seat_total = int(4) #전체 자리 수
        car_total = int(0) #현재 주차장의 차량 수 
        seat_condition1 = int(0) #1번자리의 상태
        seat_condition2 = int(0) #2번자리의 상태
        seat_condition3 = int(0) #3번자리의 상태
        seat_condition4 = int(0) #4번자리의 상태
        car_num1 = None #1번째로 들어온 차량의 번호 매칭
        car_num2 = None #2번째로 들어온 차량의 번호 매칭
        car_num3 = None #3번째로 들어온 차량의 번호 매칭
        car_num4 = None #4번째로 들어온 차량의 번호 매칭
        seat_car1 = "None1" #1번자리표시
        seat_car2 = "None2" #2번자리표시
        seat_car3 = "None3" #3번자리표시
        seat_car4 = "None4" #4번자리표시
        ac_car1 = None #사고차량 번호
        ac_car2 = None #사고차량 번호
        car_info = None  #차량의 번호
        car_status = None #차량의 상태
        seat_num = None #주차된 자리
        park = open("parkinglot.txt",'w')
        acc = open("accident.txt", 'w')
        carnn = open("CarNum.txt", 'r')
        for track in tracker.tracks:
            if not track.is_confirmed() or track.time_since_update > 1:
                continue 
            bbox = track.to_tlbr()
            class_name = track.get_class()
            color = colors[int(track.track_id) % len(colors)]
            color = [i * 255 for i in color]
            cv2.rectangle(img, (int(bbox[0]), int(bbox[1])), (int(bbox[2]), int(bbox[3])), color, 2)
            cv2.rectangle(img, (int(bbox[0]), int(bbox[1]-30)), (int(bbox[0])+(len(class_name)+len(str(track.track_id)))*17, int(bbox[1])), color, -1)
            y = int(bbox[0] + bbox[2])/2.0 #객체의 중앙 좌표 y좌표
            x = int(bbox[1] + bbox[3])/2.0 #객체의 중앙 좌표 x좌표
            x = round(x)
            y = round(y)
            txt_car = carnn.read()
            if ccc == 0 and txt_car != str(None):
                carn[0] = txt_car
                ccc += 1
            if ccc == 1 and carn[0] != txt_car:
                carn[1] = txt_car
                ccc += 1
                
                
            if str(class_name) == "car":
                if str(track.track_id) == '1':
                    cv2.putText(img, class_name + "-" + "1" + "," + str(y) + "," + str(x),(int(bbox[0]), int(bbox[1]-10)),0, 0.5, (255,255,255),2)
                elif str(track.track_id) == '2':
                    cv2.putText(img, class_name + "-" + "2" + "," + str(y) + "," + str(x),(int(bbox[0]), int(bbox[1]-10)),0, 0.5, (255,255,255),2)
                elif str(track.track_id) == '3':
                    cv2.putText(img, class_name + "-" + "3" + "," + str(y) + "," + str(x),(int(bbox[0]), int(bbox[1]-10)),0, 0.5, (255,255,255),2)
                elif str(track.track_id) == '4':
                    cv2.putText(img, class_name + "-" + "4" + "," + str(y) + "," + str(x),(int(bbox[0]), int(bbox[1]-10)),0, 0.5, (255,255,255),2)
                else:
                    cv2.putText(img, class_name + "-" + str(track.track_id),(int(bbox[0]), int(bbox[1]-10)),0, 0.5, (255,255,255),2)
                if track.track_id == 1: #차량번호판과 매치시키기 수정 가능.
                    if carn[0] != None:
                        car_num1 = carn[0]
                        car_info = carn[0]
                    parking_before = 0
                    parking_after = 0
                if track.track_id == 2:
                    if carn[1] != None:
                        car_num2 = carn[1]
                        car_info = carn[1]
                    parking_before = 0
                    parking_after = 0
                if track.track_id == 3:
                    car_num3 = "3"
                if track.track_id == 4:
                    car_num4 = "4"
            ac_car1 = None
            ac_car2 = None
            if str(class_name) == "accident": #사고판단
                cv2.putText(img, class_name + "," + str(y) + "," + str(x),(int(bbox[0]), int(bbox[1]-10)),0, 0.5, (255,255,255),2)
                if ac_num == 0:
                    accident_time = datetime.datetime.now()
                ac_car1 = car_num1
                ac_car2 = car_num2
                ac_num = 1
                
            height, width, _ = img.shape
            #cv2.line(img, (0, int(3*height/6)), (width, int(3*height/6)), (0, 255, 0), thickness=2)
            #cv2.line(img, (0, 0), (0, int(3*height/6)), (0, 255, 0), thickness=2) #1번자리 left line 라인 없에도 뎀 
            #cv2.line(img, (int(3*width/12), 0), (int(3*width/12), int(3*height/6)), (0, 255, 0), thickness=2)#1번자리 right line or 2번자리 left line 라인
            #cv2.line(img, (int(6*width/12), 0), (int(6*width/12), int(3*height/6)), (0, 255, 0), thickness=2)#2번자리 right line or 3번자리 left line 라인
            #cv2.line(img, (int(9*width/12), 0), (int(9*width/12), int(3*height/6)), (0, 255, 0), thickness=2)#3번자리 right line or 4번자리 left line 라인
            #cv2.line(img, (int(12*width/12), 0), (int(12*width/12), int(3*height/6)), (0, 255, 0), thickness=2)#4번자리 right line 라인
            center_y = int(((bbox[1])+(bbox[3]))/2) #y축에대한 좌표 영역분할
            center_x = int(((bbox[0])+(bbox[2]))/2) #x축에대한 좌표 영역분할
            
            global time_a
            global time_b
            global time_c
            global time_d
            parking_before = 0
            
            if center_y <= int(3*height/6+height/30):
                if class_name == 'car':
                    car_total += 1
            
            if center_y <= int(3*height/6+height/30) and center_x <= int(3*width/12+width/30): #1번자리
                if class_name == 'car':
                    seat_condition1 += 1
                    if seat_condition1 == 0 and time_a == 3:
                        time_a = 0
                    if time_a == 0:
                        fun_a()
                    if track.track_id == 1 and time_a == 3:
                        seat_car1 = str(car_num1)
                        cv2.putText(img, 'Parking :' + car_num1, (20,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 1
                        if parking1 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 2 and time_a == 3:
                        seat_car1 = str(car_num2)
                        cv2.putText(img, 'Parking :' + car_num2, (20,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 1
                        if parking1 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 3 and time_a == 3:
                        seat_car1 = str(car_num3)
                        cv2.putText(img, 'Parking :' + car_num3, (20,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 1
                        if parking1 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 4 and time_a == 3:
                        seat_car1 = str(car_num4)
                        cv2.putText(img, 'Parking :' + car_num4, (20,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 1
                        if parking1 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
               
            if center_y <= int(3*height/6+height/30) and center_x >= int(3*width/12-width/30) and center_x <= int(6*width/12+width/30): #2번자리
                if class_name == 'car':
                    seat_condition2 += 1
                    if seat_condition2 == 0 and time_b == 3:
                        time_b = 0
                    if time_b == 0:
                        fun_b()
                    if track.track_id == 1 and time_b == 3:
                        seat_car2 = str(car_num1)
                        cv2.putText(img, 'Parking :' + car_num1, (180,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 2
                        if parking2 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 2 and time_b == 3:
                        seat_car2 = str(car_num2)
                        cv2.putText(img, 'Parking :' + car_num2, (180,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 2
                        if parking2 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 3 and time_b == 3:
                        seat_car2 = str(car_num3)
                        cv2.putText(img, 'Parking :' + car_num3, (180,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 2
                        if parking2 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 4 and time_b == 3:
                        seat_car2 = str(car_num4)
                        cv2.putText(img, 'Parking :' + car_num4, (180,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 2
                        if parking2 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
               
            if center_y <= int(3*height/6+height/30) and center_x >= int(6*width/12-width/30) and center_x <= int(9*width/12+width/30): #3번자리
                if class_name == 'car':
                    seat_condition3 += 1
                    if seat_condition3 == 0 and time_c == 3:
                        time_c = 0
                    if time_c == 0:
                        fun_c()
                    if track.track_id == 1 and time_c == 3:
                        seat_car3 = str(car_num1)
                        cv2.putText(img, 'Parking :' + car_num1, (350,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 3
                        if parking3 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 2 and time_c == 3:
                        seat_car3 = str(car_num2)
                        cv2.putText(img, 'Parking :' + car_num2, (350,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 3
                        if parking3 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 3 and time_c == 3:
                        seat_car3 = str(car_num3)
                        cv2.putText(img, 'Parking :' + car_num3, (350,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 3
                        if parking3 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 4 and time_c == 3:
                        seat_car3 = str(car_num4)
                        cv2.putText(img, 'Parking :' + car_num4, (350,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 3
                        if parking3 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                
            if center_y <= int(3*height/6+height/30) and center_x >= int(9*width/12-width/30) and center_x <= int(12*width/12+width/30): #4번자리
                if class_name == 'car':
                    seat_condition4 += 1
                    if seat_condition4 == 0 and time_d == 3:
                        time_d = 0
                    if time_d == 0:
                        fun_d()
                    if track.track_id == 1 and time_d == 3:
                        seat_car4 = str(car_num1)
                        cv2.putText(img, 'Parking :' + car_num1, (500,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 4
                        if parking4 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 2 and time_d == 3:
                        seat_car4 = str(car_num2)
                        cv2.putText(img, 'Parking :' + car_num2, (500,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 4
                        if parking4 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 3 and time_d == 3:
                        seat_car4 = str(car_num3)
                        cv2.putText(img, 'Parking :' + car_num3, (500,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 4
                        if parking4 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    elif track.track_id == 4 and time_d == 3:
                        seat_car4 = str(car_num4)
                        cv2.putText(img, 'Parking :' + car_num4, (500,300), 0, 0.4, (0,0,255),2)
                        car_status = "parkIn"
                        seat_num = 4
                        if parking4 == 0:
                            parking_before = 1
                            parking_after = 1
                        continue
                    
        cv2.putText(img, 'Seat_total : ' + str(seat_total), (0, 30), 0, 0.3, (0,0,255), 2)
        cv2.putText(img, 'Car_total : ' + str(car_total), (0, 50), 0, 0.3, (0,0,255), 2)
        cv2.putText(img, 'Seat_num1 : ' + str(seat_condition1), (20,250), 0, 0.4, (0,0,255),2) #1번자리에 대해서 주차 여부를 판단하여 화면에 표시
        cv2.putText(img, 'Seat_num2 : ' + str(seat_condition2), (180,250), 0, 0.4, (0,0,255),2) #2번자리에 대해서 주차 여부 판단하여 화면에 표시
        cv2.putText(img, 'Seat_num3 : ' + str(seat_condition3), (350,250), 0, 0.4, (0,0,255),2) #3번자리에 대해서 주차 여부 판단하여 화면에 표시
        cv2.putText(img, 'Seat_num4 : ' + str(seat_condition4), (500,250), 0, 0.4, (0,0,255),2) #4번자리에 대해서 주차 여부 판단하여 화면에 표시

        if parking_before == 0 and parking_after == 0:
            if seat_condition1 == 0 and parking1 == 1:
                if parking[0][1] == 1:
                    car_info = parking[0][0]
                    seat_num = 1
                    car_status = "parkOut"
                elif parking[1][1] == 1:
                    car_info = parking[1][0]
                    seat_num = 1
                    car_status = "parkOut"
            elif seat_condition2 == 0 and parking2 == 1:
                if parking[0][1] == 2:
                    car_info = parking[0][0]
                    seat_num = 2
                    car_status = "parkOut"
                elif parking[1][1] == 2:
                    car_info = parking[1][0]
                    seat_num = 2
                    car_status = "parkOut"
            elif seat_condition3 == 0 and parking3 == 1:
                if parking[0][1] == 3:
                    car_info = parking[0][0]
                    seat_num = 3
                    car_status = "parkOut"
                elif parking[1][1] == 3:
                    car_info = parking[1][0]
                    seat_num = 3
                    car_status = "parkOut"
            elif seat_condition4 == 0 and parking4 == 1:
                if parking[0][1] == 4:
                    car_info = parking[0][0]
                    seat_num = 4
                    car_status = "parkOut"
                elif parking[1][1] == 4:
                    car_info = parking[1][0]
                    seat_num = 4
                    car_status = "parkOut"
                
        elif parking_before == 1 and parking_after == 1:
            if seat_num == 1:
                if co == 0 and parking1 == 0:
                    parking[0][0] = car_info
                    parking[0][1] = seat_num
                    co += 1
                    parking1 += 1
                elif co == 1 and parking1 == 0:
                    parking[1][0] = car_info
                    parking[1][1] = seat_num
                    parking1 += 1
            if seat_num == 2:
                if co == 0 and parking2 == 0:
                    parking[0][0] = car_info
                    parking[0][1] = seat_num
                    co += 1
                    parking2 += 1
                elif co == 1 and parking2 == 0:
                    parking[1][0] = car_info
                    parking[1][1] = seat_num
                    parking2 += 1
            if seat_num == 3:
                if co == 0 and parking3 == 0:
                    parking[0][0] = car_info
                    parking[0][1] = seat_num
                    co += 1
                    parking3 += 1
                elif co == 1 and parking3 == 0:
                    parking[1][0] = car_info
                    parking[1][1] = seat_num
                    parking3 += 1
            if seat_num == 4:
                if co == 0 and parking4 == 0:
                    parking[0][0] = car_info
                    parking[0][1] = seat_num
                    co += 1
                    parking4 += 1
                elif co == 1 and parking4 == 0:
                    parking[1][0] = car_info
                    parking[1][1] = seat_num
                    parking4 += 1
                
        data = str(ac_car1) + " " + str(ac_car2) + " " +str(accident_time) + "\n" #사고난 시간 및 차량 번호 기록
        acc.write(data)
        data = str(car_info) + " " + str(seat_num) + " " + str(car_status)+ "\n" #차량 정보 자리정보 주차유무 기록
        park.write(data)
        acc.close()
        park.close()
        carnn.close()
 
        #if (re_carinfo == car_info) and (re_carstatus == car_status) and (re_seatnum == seat_num):
        #    print(0)
        #    continue
        #elif (re_carinfo != car_info) or (re_carstatus != car_status) or (re_seatnum != seat_num):
        #    print(1)
            #re_carinfo = car_info
            #re_carstatus = car_status
            #re_seatnum = seat_num
            #comm1 = {'Content-Type': 'application/json; charset=utf-8'}
            #param1 = {
            #  'carNum' : parkinfo[0],
            #  'seatNum' : parkinfo[1],
            #  'status' : parkinfo[2],
            #}
            #respone1 = requests.post(url1, headers=comm1, data=json.dumps(param1))
            
        
        ### UNCOMMENT BELOW IF YOU WANT CONSTANTLY CHANGING YOLO DETECTIONS TO BE SHOWN ON SCREEN
        #for det in detections:
        #    bbox = det.to_tlbr() 
        #    cv2.rectangle(img,(int(bbox[0]), int(bbox[1])), (int(bbox[2]), int(bbox[3])),(255,0,0), 2)
        
        # print fps on screen 
        fps  = ( fps + (1./(time.time()-t1)) ) / 2
        cv2.putText(img, "FPS: {:.2f}".format(fps), (0, 10),
                          cv2.FONT_HERSHEY_COMPLEX_SMALL, 0.5, (0, 0, 255), 2)
        cv2.imshow('output', img)
        if FLAGS.output:
            out.write(img)
            frame_index = frame_index + 1
            list_file.write(str(frame_index)+' ')
            if len(converted_boxes) != 0:
                for i in range(0,len(converted_boxes)):
                    list_file.write(str(converted_boxes[i][0]) + ' '+str(converted_boxes[i][1]) + ' '+str(converted_boxes[i][2]) + ' '+str(converted_boxes[i][3]) + ' ')
            list_file.write('\n')

        # press q to quit
        if cv2.waitKey(1) == ord('q'):
            break
    vid.release()
    if FLAGS.ouput:
        out.release()
        list_file.close()
    cv2.destroyAllWindows()


if __name__ == '__main__':
    try:
        app.run(main)
    except SystemExit:
        pass
