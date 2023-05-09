from pyspark.ml.feature import VectorAssembler, StringIndexer
from pyspark.sql.types import DoubleType

def run(spark, predict_data, model):
	input_data = predict_data.select("payload")
	dataframe = spark.read.json(input_data.rdd.map(lambda r: r.payload))

	for col_name in dataframe.columns:
		test = dataframe.withColumn(col_name, dataframe[col_name].cast(DoubleType()))
		if test.where(test[col_name].isNull()).count() == dataframe.count():
			indexer = StringIndexer(inputCol=col_name, outputCol=col_name+"Index")
			dataframe = indexer.fit(dataframe).transform(dataframe).drop(col_name)
		else:
			dataframe = test

	cols = dataframe.columns
	assembler = VectorAssembler(inputCols=cols, outputCol="features")
	features_data = assembler.transform(dataframe)
	prediction = model.transform(features_data)
	output = prediction.select("prediction").collect()[0].prediction
	print("Prediction: %d" %output)
	print("Probability: %s" %prediction.select("probability").collect()[0].probability)
	return output