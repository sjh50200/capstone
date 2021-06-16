import pytesseract
from PIL import Image
print("convert image to string")
print(pytesseract.image_to_string(Image.open("plate.png")));
