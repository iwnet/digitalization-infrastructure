.PHONY: \
	build \
	clean \
	hadoop_base \
	hadoop_namenode \
	hadoop_datanode \
	hadoop_yarn_resourcemanager \
	hadoop_yarn_nodemanager \
	spark \
	zookeeper \
	hbase_base \
	hbase_master \
	hbase_regionserver \
	postgres \
	livy \
	bda \
	keycloak \
	kafka


build: \
	hadoop_base \
	hadoop_namenode \
	hadoop_datanode \
	hadoop_yarn_resourcemanager \
	hadoop_yarn_nodemanager \
	spark \
	zookeeper \
	hbase_base \
	hbase_master \
	hbase_regionserver \
	postgres \
	livy \
	bda \
	keycloak \
	kafka


include .env
export

hadoop_base:
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--tag iwnet/hadoop-base-$(HADOOP_VERSION) \
		./hadoop/base

hadoop_namenode: hadoop_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--tag iwnet/hadoop-namenode-$(HADOOP_VERSION) \
		./hadoop/namenode

hadoop_datanode: hadoop_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--tag iwnet/hadoop-datanode-$(HADOOP_VERSION) \
		./hadoop/datanode

hadoop_yarn_resourcemanager: hadoop_base 
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--tag iwnet/hadoop-yarn-resourcemanager-$(HADOOP_VERSION) \
		./hadoop/resourcemanager

hadoop_yarn_nodemanager: hadoop_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--tag iwnet/hadoop-yarn-nodemanager-$(HADOOP_VERSION) \
		./hadoop/nodemanager

spark: hadoop_yarn_nodemanager
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--build-arg SPARK_VERSION=$(SPARK_VERSION) \
                --build-arg HBASE_VERSION=$(HBASE_VERSION) \
		--tag iwnet/spark-$(SPARK_VERSION) \
		./spark

zookeeper: hadoop_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--build-arg ZOOKEEPER_VERSION=$(ZOOKEEPER_VERSION) \
		--tag iwnet/zookeeper-$(ZOOKEEPER_VERSION) \
		./zookeeper

hbase_base: hadoop_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--build-arg HBASE_VERSION=$(HBASE_VERSION) \
		--tag iwnet/hbase-base-$(HBASE_VERSION) \
		./hbase/base

hbase_master: hbase_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--build-arg HBASE_VERSION=$(HBASE_VERSION) \
		--tag iwnet/hbase-master-$(HBASE_VERSION) \
		./hbase/master

hbase_regionserver: hbase_base
	docker build \
		--build-arg HADOOP_VERSION=$(HADOOP_VERSION) \
		--build-arg HBASE_VERSION=$(HBASE_VERSION) \
		--tag iwnet/hbase-regionserver-$(HBASE_VERSION) \
		./hbase/regionserver

postgres:
	docker build \
		--tag iwnet/postgres-11 \
		./postgres

livy: spark
	docker build \
		--build-arg SPARK_VERSION=$(SPARK_VERSION) \
		--build-arg LIVY_VERSION=$(LIVY_VERSION) \
		--tag iwnet/livy-$(LIVY_VERSION) \
		./livy

bda: spark
	docker build \
		--build-arg SPARK_VERSION=$(SPARK_VERSION) \
		--file ./bda/Dockerfile \
		--tag iwnet/bda \
		./bda

keycloak: 
	docker build \
                --build-arg KEYCLOAK_VERSION=$(KEYCLOAK_VERSION) \
                --file ./keycloak/Dockerfile \
                --tag iwnet/keycloak \
                ./keycloak

kafka:
	docker build \
		--build-arg kafka_version=$(KAFKA_VERSION) \
		--file ./kafka/Dockerfile \
		--tag iwnet/kafka-$(KAFKA_VERSION) \
		./kafka


clean:
	docker rmi \
		iwnet/hadoop-base-$(HADOOP_VERSION) \
		iwnet/hadoop-namenode-$(HADOOP_VERSION) \
		iwnet/hadoop-datanode-$(HADOOP_VERSION) \
		iwnet/hadoop-yarn-resourcemanager-$(HADOOP_VERSION) \
		iwnet/hadoop-yarn-nodemanager-$(HADOOP_VERSION) \
		iwnet/spark-$(SPARK_VERSION) \
		iwnet/zookeeper-$(ZOOKEEPER_VERSION) \
		iwnet/hbase-base-$(HBASE_VERSION) \
		iwnet/hbase-master-$(HBASE_VERSION) \
		iwnet/hbase-regionserver-$(HBASE_VERSION) \
		iwnet/postgres-11 \
		iwnet/livy-$(LIVY_VERSION) \
		iwnet/bda \
		iwnet/keycloak \
                iwnet/kafka-$(KAFKA_VERSION)

