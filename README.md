# Heiroglyphics Translator

An application to identify separate heiroglyphic symbols.

Algorithm:
An image (that contains the heiroglyphs to be translated) is taken as an input,then the image will be segmented to cut every heiroglyph into a seperate image, every image is taken and compared to images in the dataset to find the best match of the image 


This repo Contains:

Dictionary_Translation.py       Functions to translate the gardiner label to english 

FinalWorking.py                 The complete algorithm from uploading image to english translation

Final_classifier.py             Classifier Model training 

Dataset.txt                     Dictionary of labels and english words

Label_dict.py                   Dictionary For mapping the labels to integers to be used in classifier

Model.h5                        The trained model on the dataset

Application:
FinalWorkingFileWithServer.py   The complete algorithm and the server side 
