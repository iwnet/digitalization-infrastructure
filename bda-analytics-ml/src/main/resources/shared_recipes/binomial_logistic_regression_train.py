from pyspark.ml.classification import LogisticRegression
from pyspark.ml.feature import VectorAssembler, StringIndexer
from pyspark.sql.types import DoubleType

def run(spark, training_data, label_column, maxIter, regParam, elasticNetParam):
	input_data = training_data.select("payload")
	dataframe = spark.read.json(input_data.rdd.map(lambda r: r.payload))

	for col_name in dataframe.columns:
		test = dataframe.withColumn(col_name, dataframe[col_name].cast(DoubleType()))
		if test.where(test[col_name].isNull()).count() == dataframe.count():
			indexer = StringIndexer(inputCol=col_name, outputCol=col_name+"Index")
			dataframe = indexer.fit(dataframe).transform(dataframe).drop(col_name)
		else:
			dataframe = test

	cols = dataframe.drop(label_column).columns
	assembler = VectorAssembler(inputCols=cols, outputCol="features")
	features_data = assembler.transform(dataframe)
	data = features_data.withColumnRenamed(label_column, "label").select("features", "label").cache()
	lr = LogisticRegression(featuresCol = 'features', labelCol = 'label', maxIter=int(maxIter), regParam=float(regParam), elasticNetParam=float(elasticNetParam))

	# Fit the model
	lrModel = lr.fit(data)
	return lrModel