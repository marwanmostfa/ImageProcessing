import json

data = {} #global variable

def translation_to_dictionary(): #Create Dictionary from the dataset
    filename = 'dataset.txt'

    with open(filename) as fh:
        for line in fh:
            d, description = line.strip().split(';', 1)
            data[d] = description.strip()

    return json.dumps(data, indent=2, sort_keys=True) #return dictionary: data


def labels_to_english(label_list): #Get the english translation of the label

    out=[]
    for label in label_list:
        for key in data:
            if key==label:
                out.append(data[key]) 
            
            else:
                continue
    return out

translation_to_dictionary()
labels_to_english(label_list)
