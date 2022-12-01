from FruitModel import FruitModel

if __name__ == "__main__":
    fruitModel = FruitModel(imageWidth=32, imageHeight=32, batchSize=20)
    # 460 images; 70%
    fruitModel.prepareTrainingDataSet("../data/fruits/train")
    # 66 images; 10%
    fruitModel.prepareValidationgDataSet("../data/fruits/validation")
    # 130 images; 20%
    fruitModel.prepareTestDataSet("../data/fruits/test")
    fruitModel.setUpModel()
    fruitModel.compileModel()
    fruitModel.trainModel()

    #### EXPORT
    #fruitModel.exporToTfLite("../model/fruitModel.tflite")

    #### TESTING
    # Display the imported training Set
    # fruitModel.displayImportedTrainingDataset()

    # Display images with their classification
    # fruitModel.displayDatasetClassification()

    # Display text with their classification
    # fruitModel.showTextDatasetClassification()
    
    # Evalue
    fruitModel.evaluateModel()