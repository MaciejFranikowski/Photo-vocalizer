from FruitModel import FruitModel

if __name__ == "__main__":
    fruitModel = FruitModel(imageWidth=32, imageHeight=32, batchSize=20)
    fruitModel.prepareTrainingDataSet("../data/fruits/train")
    fruitModel.prepareValidationgDataSet("../data/fruits/validation")
    fruitModel.prepareTestDataSet("../data/fruits/train")
    fruitModel.setUpModel()
    fruitModel.compileModel()
    fruitModel.trainModel()

    #### TESTING

    # Display the imported training Set
    # fruitModel.displayImportedTrainingDataset()

    # Display images with their classification
    #fruitModel.displayDatasetClassification()
    
    # Evalue
    #fruitModel.evaluateModel()