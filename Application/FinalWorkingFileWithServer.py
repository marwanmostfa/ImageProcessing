# -*- coding: utf-8 -*-
"""
To use this file add it to the src directory in this repo:
    https://github.com/morrisfranken/glyphreader
"""

# Commented out IPython magic to ensure Python compatibility.
import cv2
import base64
import sys
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import copy
import socket
import os
import shutil
import tensorflow
import json
import keras
#import sklearn
import scipy
#import pyyaml
import h5py
from google.cloud import storage
#from firebase import firebase
from classify import classifier

# %matplotlib inline

##Create function to loop over array of images and shows it
def imshow_array(array):
  for i in range(len(array)):
    plt.subplot(1,len(array),i+1),plt.imshow(array[i],'gray')
    plt.xticks([]),plt.yticks([])

def edge_detection(im):
##Sample and smooth image first
  cv2.pyrUp(im,im)
  im = cv2.GaussianBlur(im,(5,5),5)
##find the OTSU threshold and then apply the canny edge detector using the threshold
  th,bw1 = cv2.threshold(im, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
  bw1 = cv2.Canny(im, th, th/2,True,5)
  bw2 = cv2.Canny(im, th, th/2,True,3)
  
  ##using hitmiss algorithm to link broken edges
  k = np.array([[0, 0, 1],
              [0, -1, 0],
              [1, 0, 0]])
  hitmiss = cv2.morphologyEx(bw1, cv2.MORPH_HITMISS, k)
  bw1 |= hitmiss
  kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(3, 3))
  prevMu = 0
  recons = bw2.copy()
  for i  in range(350):

    recons = cv2.dilate(recons, kernel)
    recons &= bw1
    
  return recons

## After the vertical lines detection, if the distance between the lines very small (8 pixels), then it's too small to contain a hieroglyph and deleting one of the lines
def filter_lines(lines_start_point):
  ##define constants
  small_dist_found = False
  min_dist = 8
  length = len(lines_start_point)
  new_lines_list =copy.copy(lines_start_point) 
  
  #loop over list of vertical lines detected
  for i in range(length-1): 
    x_dist = lines_start_point[i+1]-lines_start_point[i] 
  
    #repleace one of the lines with mid distance between the 2 lines and delete the other line
    if x_dist <= min_dist:
      small_dist_found = True
      x_mid = (lines_start_point[i+1]+lines_start_point[i])/2 
      new_lines_list[i]=x_mid
      new_lines_list[i+1] =-1

   ##marking the lines to be deleted -1 then deleting it   
  while -1 in new_lines_list: new_lines_list.remove(-1)
  if small_dist_found: ##repeat the process to check that there's no lines with less than 8 ppixel distance
    new_lines_list = filter_lines(new_lines_list)
  return new_lines_list

def vertical_line_detection(hiero):
  #morphological function to emphasis the vertical lines
  kernel = np.ones((5,5),np.uint8) 
  temp1 = cv2.morphologyEx(hiero, cv2.MORPH_BLACKHAT, kernel)
  temp2 = cv2.medianBlur(temp1,5)
  temp3 = cv2.Canny(temp2,0,255,L2gradient=True)
  image_height = hiero.shape[0]

  #hough transform to detect vertical edges
  lines = cv2.HoughLines(temp3,1,np.pi/180, int(image_height/6))
  my_copy = copy.copy(hiero)
  lines_start_point = []
  #draw each line detected
  for line in lines:    
    for rho,theta in line:
      if theta != 0:
        continue
      a = np.cos(theta)
      b = np.sin(theta)
      x0 = a*rho
      y0 = b*rho
      x1 = int(x0 + 1000*(-b))
      y1 = int(y0 + 1000*(a))
      x2 = int(x0 - 1000*(-b))
      y2 = int(y0 - 1000*(a))
      cv2.line(my_copy,(x1,y1),(x2,y2),(255,0,0),2)
      lines_start_point.append(x1)
  lines_start_point.sort()
  ##filter lines if distance between 2 lines less than 8 pixels
  listos = filter_lines(lines_start_point)
  final_lines_drawn = copy.copy(hiero)
  ##draw lines after filtering
  y = final_lines_drawn.shape[0]
  for x in listos:
    cv2.line(final_lines_drawn,(int(x),0),(int(x),y),(255,0,0),2)
  return final_lines_drawn,listos

def crop_image(image,lines): ##cut the original image into list of hieroglyphic columns
  cropped=[]
  for i in range(len(lines)-1):
    cropped.append(image[:,int(lines[i]):int(lines[i+1])])
  final_list = []
  
##check if col width less than 60 then it's not a useful one
  for i in range(len(cropped)):  
    if cropped[i].shape[1]<60:
      continue 
    final_list.append(cropped[i])
  return final_list


##simple function to apply morphological filters to array of images
def Morph_array(array,kernel,morph): 
  return_array = []
  for i in range(len(array)):
    if morph == 'dilate':
      return_array.append(cv2.dilate(array[i],kernel))
    if morph == 'erode':
      return_array.append(cv2.erode(array[i],kernel))
    if morph == 'edge':
      return_array.append(edge_detection(array[i]))
    if morph == 'new_edge':
      return_array.append(edge_new(array[i]))
    if morph == 'bilateral':
      return_array.append(cv2.bilateralFilter(array[i],5,75,75))
    if morph=='filter2d':
      return_array.append(cv2.filter2D(array[i], -1, kernel))
  return return_array

##function to draw bounding boxes on the detected hieroglyphs
def draw_rec(cropped_col,stats):
  img_copy = copy.copy(cropped_col)
  col_area = cropped_col.shape[0]*cropped_col.shape[1]
  
  dirName = r'C:\Users\user\Desktop\glyphreader-master\examples'
  #create folder to save the detected hierogplyphs cropped from the original img
  if not os.path.exists(dirName):
    os.makedirs(dirName)
    print("Directory " , dirName ,  " Created ")
  else:    
    #print("Directory " , dirName ,  " already exists")
    pass
  
  ##blocks list to contain the detected hierogplyphs
  blocks =[]
  i =0
  for left,top,width,height,area in stats:
    ##check if bounding box too big or too small
    if area<300 or area == col_area or height==cropped_col.shape[0] or width<20:
      #i +=1
      continue
    blocks.append(cropped_col[top:top+height,left:left+width])
    filename = r'C:\Users\user\Desktop\glyphreader-master\examples\img%d.jpg'%i
    cv2.imwrite(filename, blocks[i])
    cv2.rectangle(img_copy, (left,top), (left+width,top+height),(0,255,0),2) 
    i +=1
  #return blocks,img_copy

#----------------------------------------------------------------------------------------------------

HOST = '192.168.1.9'  # this is your localhost
PORT = 8888

sock =socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# Bind socket to Host and Port
try:
    sock.bind((HOST, PORT))
except socket.error as err:
    print('Bind Failed')
    sys.exit()
print('Socket Bind Success!')
sock.listen(10)
print('Socket is now listening')

#clear the directory from previous images 
folder =r'C:\Users\user\Desktop\glyphreader-master\examples'
for filename in os.listdir(folder):
    file_path = os.path.join(folder, filename)
    try:
        if os.path.isfile(file_path) or os.path.islink(file_path):
            os.unlink(file_path)
        elif os.path.isdir(file_path):
            shutil.rmtree(file_path)
    except Exception as e:
        print('Failed to delete %s. Reason: %s' % (file_path, e))
        
        
        
os.environ["GOOGLE_APPLICATION_CREDENTIALS"]=r"C:\Users\user\Desktop\hieroTranslate-d6f4ea2ab8fe.json"          
client = storage.Client()
bucket = client.get_bucket('hierotranslate.appspot.com')
bucket.exists()

flag=1
while 1:
    client, address = sock.accept()
   
    ## recive the image and start processing it
    if flag==1:    
        data = b''
        with open('tst.jpg', 'wb') as image:
            while True:
                b= client.recv(1024)  
                data += b
                if not b:
                    break
            image.write(base64.decodebytes(data)) #save image in tst.jpg           
            print ('received, yay!')
            img=cv2.imread("tst.jpg", 1)
            
            kernel = np.ones((3,3),np.uint8)
            kerneledge = np.array([[-1,-1,-1], 
                    [-1, 9,-1],
                    [-1,-1,-1]])
            #Blur with respect to egdes and sharpen the image with less noise
            dst = cv2.bilateralFilter(img,5,75,75)
            sharpend = cv2.filter2D(dst, -1, kerneledge)
            #img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            sharpend=cv2.cvtColor(sharpend, cv2.COLOR_BGR2GRAY)
            #get the edges after sharpening the image
            edge_of_sharp= edge_detection(sharpend)
            dilate = cv2.dilate(edge_of_sharp,kernel)
          
            #get vertical lines of the image to seperate the columns
            line_det,lines_list = vertical_line_detection(edge_of_sharp)
            crop_list = crop_image(sharpend,lines_list)
            imshow_array(crop_list)
            
            #get the edges of the hieroglyph symbol in each column
            edged = Morph_array(crop_list,kernel,'edge')
            edged_dilated = Morph_array(edged,kernel,'dilate')
            
            ##using connected components to extract each symbol on it's own
            
            #index = int(input('Input image index'))
            jj=0
            for index in edged_dilated:
                retval, labels, stats, centroids	=	cv2.connectedComponentsWithStats(index)
                ##to finally get the symbols of one column in a list and the column image with the bounding boxes detected
                draw_rec(crop_list[jj],stats)
                jj=jj+1
            
            #cutouts, rec = draw_rec(crop_list[index],stats)
            
            flag=0
                     
    else:       
        #el mfrod hna na5od eli tl3 fl akher w nb3tto ll mobile
        #"""with open(r'C:\Users\user\Desktop\glyphreader-master\examples\img%d.jpg'%i, 'rb') as image:
            #encode_bytes = base64.encodebytes(same_image.read())"""
            
        #calling the classifier which returns gardiner label for each Image
        glbls,imagePaths=classifier()
        i=0
        jsonList=[]
        fullMsg=''
        for lbl in glbls:
            print(*lbl)
            #img_file=open(r'C:\Users\user\Desktop\glyphreader-master\examples\img%d.jpg'%i,'rb')
            # read the image file
            #imgData = img_file.read()
            
            """# build JSON object
            outjson = {}
            outjson['img'] = str(imgData,'latin-1')
            outjson['firstLabel']=str(lbl[0])
            outjson['secondLabel']=str(lbl[1])
            outjson['thirdLabel']=str(lbl[2])
            #json_data = json.dumps(outjson)
            jsonList.append(outjson) 
            img_file.close()
            i=i+1"""
            
            imageBlob = bucket.blob("/")
            imagePath= imagePaths[i]
            i=i+1
            imageBlob = bucket.blob('img%d'%i)
            imageBlob.upload_from_filename(imagePath)
            msg=str(lbl[0])+'/'+str(lbl[1])+'/'+str(lbl[2])+'?'
            
            fullMsg=fullMsg+msg
        print(fullMsg)   
        message_bytes = fullMsg.encode('ascii')
        base64_bytes = base64.b64encode(message_bytes)
        #dataToSend=json.dumps(jsonList)
        
        client.send(base64_bytes)
        print('sent')
        flag=1

    client.close()
sock.close()

