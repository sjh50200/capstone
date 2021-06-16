
import cv2
import numpy as np
import math
import argparse
try:
    from PIL import Image
except ImportError:
    import Image
import pytesseract


def crop_image(img_box, rect, box):
    print("crop image")
    mult = 1.0
    W = rect[1][0]
    H = rect[1][1]

    Xs = [i[0] for i in box]
    Ys = [i[1] for i in box]
    x1 = min(Xs)
    x2 = max(Xs)
    y1 = min(Ys)
    y2 = max(Ys)

    rotated = False
    angle = rect[2]

    if angle < -45:
        angle+=90
        rotated = True

    center = (int((x1+x2)/2), int((y1+y2)/2))
    size = (int(mult*(x2-x1)),int(mult*(y2-y1)))

    M = cv2.getRotationMatrix2D((size[0]/2, size[1]/2), angle, 1.0)

    cropped = cv2.getRectSubPix(img_box, size, center)
    cropped = cv2.warpAffine(cropped, M, size)

    croppedW = W if not rotated else H 
    croppedH = H if not rotated else W
    
    croppedRotated = cv2.getRectSubPix(cropped, (int(croppedW*mult), int(croppedH*mult)), (size[0]/2, size[1]/2))

    return croppedRotated

def compare_count(img, ctrs, x2, y2, w2, h2, old):

    erode = thresh[y2:y2+h2, x2:x2+w2]

    cv2MajorVersion = cv2.__version__.split(".")[0]
    if int(cv2MajorVersion) >= 4:
        imageContours2 = img
        contours2, hierarchy2 = cv2.findContours(erode, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    else:
        imageContours2, contours2, hierarchy2 = cv2.findContours(erode, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

    count = 0
    for i in contours2:
        chk = True
        x,y,w,h = cv2.boundingRect(i)
        if w > (h+h/10) or h / w < 0.8 or h / w > 3 or h2 / h > 2.6 or h2 / h < 1.2:# or h2 / h > 1.5:
            chk = False
        if chk == True:
            count = count + 1
            cv2.rectangle(img, (x2+x, y2+y), (x2+x+w, y2+y+h), (255,0,0), 2)
            print ("count:",count, ", h/w:", h/w, ", h2/h:", h2/h)

    #if count >= 6:
    #    cv2.imshow("subContour", imageContours2)
    return count
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", type=str, required=True, help="path to image")
args = vars(ap.parse_args())

img = cv2.imread(args["image"])

hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
hue, saturation, value = cv2.split(hsv)

adapt = cv2.adaptiveThreshold(value, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 15, 20)

kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))

topHat = cv2.morphologyEx(value, cv2.MORPH_TOPHAT, kernel)
blackHat = cv2.morphologyEx(value, cv2.MORPH_BLACKHAT, kernel)

add = cv2.add(value, topHat)
subtract = cv2.subtract(add, blackHat)

blur = cv2.GaussianBlur(subtract, (5, 5), 0)

thresh = cv2.adaptiveThreshold(blur, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 19, 9)

dilate = cv2.dilate(thresh, kernel, iterations=1)

cv2MajorVersion = cv2.__version__.split(".")[0]
if int(cv2MajorVersion) >= 4:
    contours, hierarchy = cv2.findContours(dilate, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
else:
    imageContours, contours, hierarchy = cv2.findContours(dilate, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)

height, width = thresh.shape

imageContours = np.zeros((height, width, 3), dtype=np.uint8)

possibleChars = []
countOfPossibleChars = 0

for i in contours:
    chk = True
    old = False
    if cv2.contourArea(i) < 1000:
        continue
    x,y,w,h = cv2.boundingRect(i)
    ratio = w / h
    cv2.rectangle(imageContours, (x, y), (x+w, y+h), (0,255,255), 1)
    if w <= h or ratio < 1.7 or ratio > 7:
        chk = False
    if ratio < 3:
        old = True
    if chk == True:

        rect = cv2.minAreaRect(i)
        box = cv2.boxPoints(rect)
        box = np.int0(box)


        cv2.rectangle(imageContours, (x, y), (x+w, y+h), (0,255,0), 2)
        cnt = compare_count(imageContours, contours, x,y,w,h, old)
        print ("ratio:",ratio, ", count:", cnt, ",old:",old)
        if cnt >= 6 and cnt <= 9:
            cv2.rectangle(imageContours, (x, y), (x+w, y+h), (0,0,255), 2)
            value = cv2.adaptiveThreshold(value, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 15, 19)
            img_croped = crop_image(value, rect, box)
            xoffset = int(round(w/20))
            yoffset = int(round(h/20))
            if old == True:
                crop_img = img_croped[yoffset*6 : h - yoffset*2, xoffset : w-xoffset]
            else:
                crop_img = img_croped[yoffset*2 : h - yoffset*2, xoffset*2 : w-xoffset*2]
            cv2.imshow("rotated2", crop_img)
            im = Image.fromarray(np.uint8(crop_img))
            im.save("./numberplate.png")
            text = pytesseract.image_to_string(im, lang='kor')
            print("text present in images:",text)

cv2.imshow("contours", imageContours)

imageContours = np.zeros((height, width, 3), np.uint8)

cv2.waitKey(0)
